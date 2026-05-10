package com.elderlycare.service.elder.impl;

import com.elderlycare.common.BusinessException;
import com.elderlycare.entity.elder.*;
import com.elderlycare.mapper.elder.*;
import com.elderlycare.service.common.AliyunAiService;
import com.elderlycare.service.elder.HealthAiSuggestionService;
import com.elderlycare.vo.elder.CareSuggestionVO;
import com.elderlycare.vo.elder.CareSuggestionVO.CareSuggestion;
import com.elderlycare.vo.elder.MedicalSuggestionVO;
import com.elderlycare.vo.elder.MedicalSuggestionVO.MedicalSuggestion;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 健康AI建议服务实现
 * 读缓存 + 定时生成 + 实时降级（无缓存时读旧版规则引擎）
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthAiSuggestionServiceImpl implements HealthAiSuggestionService {

    private final HealthAiSuggestionMapper suggestionMapper;
    private final ElderMapper elderMapper;
    private final ElderHealthMapper elderHealthMapper;
    private final HealthMeasurementMapper measurementMapper;
    private final AliyunAiService aliyunAiService;
    private final ObjectMapper objectMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int CACHE_DAYS = 7; // 缓存有效期7天

    @Override
    public HealthAiSuggestion getLatestSuggestion(String elderId) {
        HealthAiSuggestion suggestion = suggestionMapper.selectLatestByElderId(elderId);
        if (suggestion == null) {
            return null;
        }
        // 检查是否过期
        if (suggestion.getExpiresAt() != null && suggestion.getExpiresAt().isBefore(LocalDateTime.now())) {
            // 标记为已过期（不删除，留存历史）
            suggestion.setStatus(1);
            suggestionMapper.updateById(suggestion);
            return null;
        }
        return suggestion;
    }

    @Override
    public CareSuggestionVO getCareSuggestionVo(String elderId) {
        HealthAiSuggestion s = getLatestSuggestion(elderId);
        if (s == null) {
            return null;
        }
        return convertToCareSuggestionVo(s);
    }

    @Override
    public MedicalSuggestionVO getMedicalSuggestionVo(String elderId) {
        HealthAiSuggestion s = getLatestSuggestion(elderId);
        if (s == null) {
            return null;
        }
        return convertToMedicalSuggestionVo(s);
    }

    @Override
    @Transactional
    public void generateForElder(String elderId) {
        log.info("开始为老人 {} 生成AI健康建议", elderId);
        try {
            generateInternal(elderId);
            log.info("老人 {} AI建议生成成功", elderId);
        } catch (Exception e) {
            log.error("老人 {} AI建议生成失败: {}", elderId, e.getMessage());
            // 记录失败状态
            HealthAiSuggestion failed = new HealthAiSuggestion();
            failed.setElderId(elderId);
            failed.setElderName(elderMapper.selectById(elderId).getName());
            failed.setStatus(2); // 生成失败
            failed.setGeneratedAt(LocalDateTime.now());
            failed.setExpiresAt(LocalDateTime.now().plusDays(CACHE_DAYS));
            failed.setCreateTime(LocalDateTime.now());
            suggestionMapper.insert(failed);
        }
    }

    @Override
    @Transactional
    public int generateAll() {
        // 查询所有有效老人（ACTIVE状态）
        List<Elder> elders = elderMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Elder>()
                .eq(Elder::getStatus, "ACTIVE")
                .eq(Elder::getDeleted, 0)
        );
        int success = 0;
        for (Elder elder : elders) {
            try {
                generateInternal(elder.getElderId());
                success++;
                Thread.sleep(500); // 500ms间隔，避免API限流
            } catch (Exception e) {
                log.error("老人 {} 生成失败: {}", elder.getElderId(), e.getMessage());
            }
        }
        log.info("批量生成完成，成功 {}/{}", success, elders.size());
        return success;
    }

    /**
     * 内部生成逻辑
     */
    private void generateInternal(String elderId) throws Exception {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        // 获取健康档案
        ElderHealth health = elderHealthMapper.selectByElderId(elderId);

        // 获取最近10条测量数据（JSON格式，用于AI输入）
        List<HealthMeasurement> measurements = measurementMapper.selectByElderIdWithLimit(elderId, 10);
        String measurementsJson = buildMeasurementsJson(measurements);

        // 调用AI生成
        String aiResult = aliyunAiService.generateHealthAdvice(
                elderId, elder.getName(), elder.getAge(), elder.getGender(),
                elder.getCareLevel(), elder.getHealthStatus(),
                health != null ? health.getBloodType() : null,
                health != null ? health.getHeight() : null,
                health != null ? health.getWeight() : null,
                health != null ? health.getMedicalHistory() : null,
                health != null ? health.getAllergyHistory() : null,
                health != null ? health.getCurrentMedication() : null,
                health != null ? health.getChronicDiseases() : null,
                health != null ? health.getAdlScore() : null,
                health != null ? health.getMmseScore() : null,
                health != null ? health.getGdsScore() : null,
                health != null ? health.getFallRisk() : null,
                measurementsJson
        );

        if (aiResult == null) {
            throw new BusinessException(500, "AI服务未返回有效结果");
        }

        // 解析AI返回的JSON
        com.fasterxml.jackson.databind.JsonNode root = objectMapper.readTree(aiResult);
        com.fasterxml.jackson.databind.JsonNode careNode = root.get("careSuggestion");
        com.fasterxml.jackson.databind.JsonNode medicalNode = root.get("medicalSuggestion");

        HealthAiSuggestion suggestion = new HealthAiSuggestion();
        suggestion.setElderId(elderId);
        suggestion.setElderName(elder.getName());
        suggestion.setAiModel("qwen-plus");
        suggestion.setAiTokensUsed(0); // 暂时不统计token
        suggestion.setGeneratedAt(LocalDateTime.now());
        suggestion.setExpiresAt(LocalDateTime.now().plusDays(CACHE_DAYS));
        suggestion.setStatus(0);
        suggestion.setCreateTime(LocalDateTime.now());

        if (careNode != null) {
            suggestion.setCareLevelSuggestion(getText(careNode, "careLevelSuggestion"));
            suggestion.setRiskAlerts(toJsonArray(careNode.get("riskAlerts")));
            suggestion.setCareSuggestions(toJsonArray(careNode.get("suggestions")));
            suggestion.setCareEvaluateTime(LocalDateTime.now());
        }

        if (medicalNode != null) {
            suggestion.setUrgencyLevel(getText(medicalNode, "urgencyLevel"));
            suggestion.setUrgencyLevelName(getText(medicalNode, "urgencyLevelName"));
            suggestion.setSuggestedDepartment(getText(medicalNode, "suggestedDepartment"));
            suggestion.setSymptoms(toJsonArray(medicalNode.get("symptoms")));
            suggestion.setMedicalSuggestions(toJsonArray(medicalNode.get("suggestions")));
            suggestion.setMedicalEvaluateTime(LocalDateTime.now());
        }

        // 插入或更新（覆盖旧记录）
        HealthAiSuggestion existing = suggestionMapper.selectLatestByElderId(elderId);
        if (existing != null) {
            suggestion.setId(existing.getId());
            suggestionMapper.updateById(suggestion);
        } else {
            suggestionMapper.insert(suggestion);
        }

        // 生成语音播报（不阻塞主流程）
        generateAudio(suggestion, elder);
    }

    /**
     * 生成AI建议语音播报（qwen-tts）
     */
    private void generateAudio(HealthAiSuggestion suggestion, Elder elder) {
        try {
            StringBuilder text = new StringBuilder();
            text.append("您好，").append(elder.getName()).append("家属。");
            if (suggestion.getCareLevelSuggestion() != null) {
                text.append("护理等级建议：").append(suggestion.getCareLevelSuggestion()).append("。");
            }
            if (suggestion.getRiskAlerts() != null && !suggestion.getRiskAlerts().isBlank()) {
                text.append("风险预警：").append(suggestion.getRiskAlerts()).append("。");
            }

            // 构建简短播报文本（建议总长150字以内）
            String speechText = text.toString();
            if (speechText.length() > 180) {
                speechText = speechText.substring(0, 180);
            }

            String audioUrl = aliyunAiService.textToSpeech(speechText, "zhixiaoyao", "mp3");
            if (audioUrl != null) {
                suggestion.setAudioUrl(audioUrl);
                suggestionMapper.updateById(suggestion);
                log.info("AI建议语音生成成功，老人: {}", elder.getName());
            }
        } catch (Exception e) {
            log.warn("语音生成失败，不影响建议内容: {}", e.getMessage());
        }
    }

    private String buildMeasurementsJson(List<HealthMeasurement> measurements) {
        if (measurements == null || measurements.isEmpty()) {
            return "[]";
        }
        try {
            List<java.util.Map<String, String>> list = new ArrayList<>();
            for (HealthMeasurement m : measurements) {
                java.util.Map<String, String> item = new java.util.HashMap<>();
                item.put("type", m.getMeasurementType());
                item.put("value", m.getMeasurementValue());
                item.put("time", m.getMeasuredAt() != null ? m.getMeasuredAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : "");
                list.add(item);
            }
            return objectMapper.writeValueAsString(list);
        } catch (Exception e) {
            return "[]";
        }
    }

    private String getText(com.fasterxml.jackson.databind.JsonNode node, String field) {
        com.fasterxml.jackson.databind.JsonNode n = node.get(field);
        return n != null && !n.isNull() ? n.asText() : null;
    }

    private String toJsonArray(com.fasterxml.jackson.databind.JsonNode node) {
        if (node == null || node.isNull()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            return "[]";
        }
    }

    // ========== 缓存转VO ==========

    public CareSuggestionVO convertToCareSuggestionVo(HealthAiSuggestion s) {
        CareSuggestionVO vo = new CareSuggestionVO();
        vo.setElderId(s.getElderId());
        vo.setElderName(s.getElderName());
        vo.setCareLevelSuggestion(s.getCareLevelSuggestion());
        vo.setEvaluateTime(s.getCareEvaluateTime() != null ? s.getCareEvaluateTime().format(FORMATTER) : null);

        if (s.getRiskAlerts() != null) {
            try {
                vo.setRiskAlerts(objectMapper.readValue(s.getRiskAlerts(), new TypeReference<List<String>>() {}));
            } catch (Exception e) { vo.setRiskAlerts(new ArrayList<>()); }
        } else {
            vo.setRiskAlerts(new ArrayList<>());
        }

        if (s.getCareSuggestions() != null) {
            try {
                vo.setSuggestions(objectMapper.readValue(s.getCareSuggestions(), new TypeReference<List<CareSuggestion>>() {}));
            } catch (Exception e) { vo.setSuggestions(new ArrayList<>()); }
        } else {
            vo.setSuggestions(new ArrayList<>());
        }

        vo.setAudioUrl(s.getAudioUrl());
        return vo;
    }

    public MedicalSuggestionVO convertToMedicalSuggestionVo(HealthAiSuggestion s) {
        MedicalSuggestionVO vo = new MedicalSuggestionVO();
        vo.setElderId(s.getElderId());
        vo.setElderName(s.getElderName());
        vo.setUrgencyLevel(s.getUrgencyLevel());
        vo.setUrgencyLevelName(s.getUrgencyLevelName());
        vo.setSuggestedDepartment(s.getSuggestedDepartment());
        vo.setEvaluateTime(s.getMedicalEvaluateTime() != null ? s.getMedicalEvaluateTime().format(FORMATTER) : null);

        if (s.getSymptoms() != null) {
            try {
                vo.setSymptoms(objectMapper.readValue(s.getSymptoms(), new TypeReference<List<String>>() {}));
            } catch (Exception e) { vo.setSymptoms(new ArrayList<>()); }
        } else {
            vo.setSymptoms(new ArrayList<>());
        }

        if (s.getMedicalSuggestions() != null) {
            try {
                vo.setSuggestions(objectMapper.readValue(s.getMedicalSuggestions(), new TypeReference<List<MedicalSuggestion>>() {}));
            } catch (Exception e) { vo.setSuggestions(new ArrayList<>()); }
        } else {
            vo.setSuggestions(new ArrayList<>());
        }

        return vo;
    }
}