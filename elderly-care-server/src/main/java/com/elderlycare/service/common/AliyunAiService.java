package com.elderlycare.service.common;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * 阿里云百炼AI服务
 * 支持：文本生成(qwen-plus)、文生图(wan2.2-t2i-plus)、图片理解(qwen-vl-plus)、语音合成(qwen-tts)
 * 底层统一走 OpenAI Compatible 接口，Bearer Token 认证
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AliyunAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${aliyun.bailian.api-key:}")
    private String apiKey;

    @Value("${aliyun.bailian.model:qwen-plus}")
    private String model;

    @Value("${aliyun.bailian.max-tokens:2000}")
    private int maxTokens;

    private static final String API_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions";

    // ==================== 文本生成：健康建议 ====================

    /**
     * 生成健康建议（护理建议 + 就医建议）
     * 使用结构化prompt + few-shot示例，让AI输出更专业、更符合医学规范
     */
    public String generateHealthAdvice(
            String elderId, String elderName, Integer age, String gender,
            String careLevel, String healthStatus,
            Integer bloodType, Double height, Double weight,
            String medicalHistory, String allergyHistory, String currentMedication, String chronicDiseases,
            Integer adlScore, Integer mmseScore, Integer gdsScore, Integer fallRisk,
            String measurementsJson
    ) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("阿里云百炼API Key未配置，跳过AI生成");
            return null;
        }

        String systemPrompt = buildSystemPrompt();
        String userPrompt = buildUserPrompt(elderId, elderName, age, gender, careLevel, healthStatus,
                bloodType, height, weight, medicalHistory, allergyHistory, currentMedication, chronicDiseases,
                adlScore, mmseScore, gdsScore, fallRisk, measurementsJson);

        int retryCount = 0;
        while (retryCount < 3) {
            try {
                String response = chatCompletion(systemPrompt, userPrompt, model, maxTokens, 0.3);
                return parseHealthAdviceResponse(response);
            } catch (HttpClientErrorException e) {
                if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                    log.warn("百炼API限流，{}ms后重试", 5000 * (retryCount + 1));
                    try { Thread.sleep(5000L * (retryCount + 1)); } catch (InterruptedException ignored) {}
                    retryCount++;
                } else if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                    log.error("百炼API Key无效: {}", e.getMessage());
                    return null;
                } else {
                    throw e;
                }
            } catch (Exception e) {
                log.error("百炼API调用失败: {}", e.getMessage());
                if (retryCount == 2) return null;
                retryCount++;
            }
        }
        return null;
    }

    // ==================== 文生图：wan2.2-t2i-plus ====================

    /**
     * 生成图片（用于健康报告封面、适老化配图等）
     * @param prompt 中文描述 prompt
     * @param model  模型名：wan2.2-t2i-plus 或 wan2.2-t2i-flash
     * @return 图片URL
     */
    public String generateImage(String prompt, String model) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("API Key未配置，跳过图片生成");
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", model != null ? model : "wan2.2-t2i-plus");
            body.put("prompt", prompt);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    "https://dashscope.aliyuncs.com/compatible-mode/v1/images/generations",
                    HttpMethod.POST, entity, String.class
            );

            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode data = root.get("data");
            if (data != null && data.isArray() && data.size() > 0) {
                JsonNode urlNode = data.get(0).get("url");
                if (urlNode == null) urlNode = data.get(0).get("b64_json");
                return urlNode != null ? urlNode.asText() : null;
            }
            return null;
        } catch (Exception e) {
            log.error("文生图失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 图片理解：qwen-vl-plus ====================

    /**
     * 识别图片内容（药品、化验单、病历等）
     * @param imageInput 图片URL或base64
     * @param prompt     任务描述
     * @return 结构化文本描述
     */
    public String understandImage(String imageInput, String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("API Key未配置，跳过图片理解");
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> msgContent = new HashMap<>();
            // 判断是URL还是base64
            if (imageInput.startsWith("http")) {
                msgContent.put("type", "image_url");
                msgContent.put("image_url", Map.of("url", imageInput));
            } else {
                msgContent.put("type", "image_url");
                msgContent.put("image_url", Map.of("url", "data:image/jpeg;base64," + imageInput));
            }

            List<Map<String, Object>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", new Object[]{
                    msgContent,
                    Map.of("type", "text", "text", prompt)
            }));

            Map<String, Object> body = new HashMap<>();
            body.put("model", "qwen-vl-plus");
            body.put("messages", messages);
            body.put("max_tokens", 1024);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    API_URL, HttpMethod.POST, entity, String.class
            );

            return parseContentFromResponse(resp.getBody());
        } catch (Exception e) {
            log.error("图片理解失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 语音合成：qwen-tts ====================

    /**
     * 文本转语音
     * @param text      待朗读文本（建议150字以内）
     * @param voice     音色：zhixiaoyao(知瑶-温柔女声)、zhicheng(知城-温暖男声)
     * @param outputFormat 输出格式：mp3
     * @return 音频URL或base64
     */
    public String textToSpeech(String text, String voice, String outputFormat) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("API Key未配置，跳过语音合成");
            return null;
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            Map<String, Object> body = new HashMap<>();
            body.put("model", "qwen-tts");
            body.put("input", Map.of("text", text));
            body.put("voice", voice != null ? voice : "zhixiaoyao");
            body.put("output_format", Map.of("format", outputFormat != null ? outputFormat : "mp3"));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> resp = restTemplate.exchange(
                    "https://dashscope.aliyuncs.com/compatible-mode/v1/audio/speech",
                    HttpMethod.POST, entity, String.class
            );

            // TTS返回的是二进制音频，编码为base64
            // 这里返回的是假URL，实际前端应直接播放响应体
            // 为兼容现有架构，返回 data:audio/mp3;base64,... 格式
            JsonNode root = objectMapper.readTree(resp.getBody());
            JsonNode audioNode = root.get("data");
            if (audioNode != null) {
                return "data:audio/mp3;base64," + audioNode.asText();
            }
            return null;
        } catch (Exception e) {
            log.error("语音合成失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 通用Chat Completion ====================

    /**
     * 通用对话接口
     */
    public String chat(String userMessage, String modelName) {
        return chat("你是一位专业的养老服务助手。", userMessage,
                modelName != null ? modelName : this.model, 1500, 0.3);
    }

    public String chat(String systemPrompt, String userMessage, String modelName, int maxTokens, double temperature) {
        if (apiKey == null || apiKey.isBlank()) {
            return null;
        }
        try {
            return chatCompletion(systemPrompt, userMessage, modelName, maxTokens, temperature);
        } catch (Exception e) {
            log.error("Chat调用失败: {}", e.getMessage());
            return null;
        }
    }

    public boolean isApiKeyValid() {
        if (apiKey == null || apiKey.isBlank()) return false;
        try {
            chatCompletion("你是一位助手。", "回复OK", this.model, 10, 0.1);
            return true;
        } catch (Exception e) {
            log.warn("百炼API Key验证失败: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 内部方法 ====================

    /**
     * 构建系统提示词：老年人健康评估专家角色
     */
    private String buildSystemPrompt() {
        return """
                你是一位资深老年医学与养老护理专家，拥有10年以上的老年健康评估经验。
                你的职责是为居家养老场景下的老年人提供专业、循证的护理建议和就医指导。

                【专业背景】
                - 熟悉《中国老年人健康管理指南》《老年医学》《居家养老服务规范》
                - 擅长老年综合征评估（跌倒、压疮、认知障碍、营养不良、尿失禁、谵妄）
                - 了解老年人常用药物的副作用和相互作用
                - 熟悉老年患者就医路径和科室选择

                【输出规范】
                1. 必须返回严格合法的JSON，不要有任何markdown代码块包裹
                2. 所有字段都要有值，不允许null值（用"暂无数据"填充字符串字段，用空数组[]填充列表字段）
                3. 建议要具体、可操作，结合老人实际情况
                4. 风险预警要明确等级（高/中/低）
                5. 优先关注危及生命的紧急情况
                """;
    }

    /**
     * 构建用户提示词：老人健康数据 + Few-shot示例
     */
    private String buildUserPrompt(
            String elderId, String elderName, Integer age, String gender,
            String careLevel, String healthStatus,
            Integer bloodType, Double height, Double weight,
            String medicalHistory, String allergyHistory, String currentMedication, String chronicDiseases,
            Integer adlScore, Integer mmseScore, Integer gdsScore, Integer fallRisk,
            String measurementsJson
    ) {
        StringBuilder sb = new StringBuilder();
        sb.append("请分析以下老年人的健康数据，生成护理建议和就医建议。\n\n");
        sb.append("【基本信息】\n");
        sb.append("- 姓名：").append(elderName).append("\n");
        sb.append("- 年龄：").append(age).append("岁\n");
        sb.append("- 性别：").append("MALE".equals(gender) ? "男" : "女").append("\n");
        sb.append("- 护理等级：").append(switch (careLevel) {
            case "HIGH" -> "一级（高级护理）";
            case "MEDIUM" -> "二级（中级护理）";
            case "NORMAL" -> "三级（普通护理）";
            default -> "未分级";
        }).append("\n");
        sb.append("- 健康状态：").append(switch (healthStatus) {
            case "GOOD" -> "良好";
            case "FAIR" -> "一般";
            case "POOR" -> "较差";
            default -> "未知";
        }).append("\n");

        if (bloodType != null) {
            String[] bt = {"未知", "A型", "B型", "O型", "AB型"};
            sb.append("- 血型：").append(bloodType >= 0 && bloodType <= 3 ? bt[bloodType] : "未知").append("\n");
        }
        if (height != null && weight != null) {
            double bmi = weight / ((height / 100.0) * (height / 100.0));
            sb.append(String.format("- 身高体重：%.0fcm / %.1fkg，BMI=%.1f（%s）\n",
                    height, weight, bmi,
                    bmi < 18.5 ? "偏瘦" : bmi < 24 ? "正常" : bmi < 28 ? "偏胖" : "肥胖"));
        }

        sb.append("\n【既往病史】").append(medicalHistory != null && !medicalHistory.isBlank() ? medicalHistory : "无").append("\n");
        sb.append("【过敏史】").append(allergyHistory != null && !allergyHistory.isBlank() ? allergyHistory : "无").append("\n");
        sb.append("【当前用药】").append(currentMedication != null && !currentMedication.isBlank() ? currentMedication : "无用药记录").append("\n");
        sb.append("【慢性病】").append(chronicDiseases != null && !chronicDiseases.isBlank() ? chronicDiseases : "无").append("\n");

        sb.append("\n【功能评估量表】\n");
        if (adlScore != null) sb.append("- ADL日常生活能力：").append(adlScore).append("分（满分100，<40分为重度依赖，40-60分为中度依赖，>60分基本自理）\n");
        if (mmseScore != null) sb.append("- MMSE认知功能：").append(mmseScore).append("分（满分30，<18分重度认知障碍，18-23分轻度认知障碍，24-26分轻度认知减退，>26分正常）\n");
        if (gdsScore != null) sb.append("- GDS抑郁量表：").append(gdsScore).append("分（<10分无抑郁，10-13分轻度抑郁，14-24分中重度抑郁，>24分重度抑郁）\n");
        sb.append("- 跌倒风险：").append(fallRisk != null ? switch (fallRisk) {
            case 0 -> "低风险";
            case 1 -> "中风险";
            case 2 -> "高风险";
            default -> "未评估";
        } : "未评估").append("\n");

        if (measurementsJson != null && !measurementsJson.isBlank() && !"null".equals(measurementsJson)) {
            sb.append("\n【近期健康检测数据】\n").append(measurementsJson).append("\n");
        }

        // Few-shot示例（让AI输出格式更稳定）
        sb.append("""
                \n【输出要求】
                请以以下JSON格式输出（直接输出JSON，不要任何额外文字）：
                {
                  "careSuggestion": {
                    "careLevelSuggestion": "护理等级建议（50字以内）",
                    "riskAlerts": ["风险预警1（高/中/低风险+具体情况）", "风险预警2"],
                    "suggestions": [
                      {
                        "type": "BLOOD_PRESSURE|BLOOD_GLUCOSE|WEIGHT|ADL|MMSE|FALL_RISK|MEDICATION|NUTRITION|OTHER",
                        "typeName": "类型中文名",
                        "content": "具体建议内容（100字以内，要结合老人实际数据）",
                        "priority": 1-4（1最紧急，4一般）,
                        "basis": "判断依据（具体数值或症状）"
                      }
                    ]
                  },
                  "medicalSuggestion": {
                    "urgencyLevel": "URGENT|WARNING|ATTENTION|NORMAL",
                    "urgencyLevelName": "紧急程度中文名",
                    "suggestedDepartment": "建议就诊科室",
                    "symptoms": ["观察到的症状1", "症状2"],
                    "suggestions": [
                      {
                        "type": "CHECKUP|BLOOD_PRESSURE|BLOOD_GLUCOSE|SPO2|OTHER",
                        "typeName": "类型中文名",
                        "content": "就医建议内容（100字以内）",
                        "priority": 1-4,
                        "basis": "判断依据"
                      }
                    ]
                  }
                }
                注意：priority数值越小越紧急（1=最紧急需立即处理，4=一般性建议）
                """);

        return sb.toString();
    }

    /**
     * 调用通用Chat Completion接口
     */
    private String chatCompletion(String systemPrompt, String userMessage, String modelName, int maxTokens, double temperature) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        messages.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("max_tokens", maxTokens);
        body.put("temperature", temperature);
        body.put("messages", messages);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                API_URL, HttpMethod.POST, entity, String.class
        );
        return response.getBody();
    }

    /**
     * 解析健康建议响应
     */
    private String parseHealthAdviceResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    String content = message.get("content").asText().trim();
                    // 去掉可能的markdown代码块
                    if (content.startsWith("```json")) content = content.substring(7);
                    else if (content.startsWith("```")) content = content.substring(3);
                    if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
                    return content.trim();
                }
            }
        } catch (Exception e) {
            log.error("解析AI健康建议响应失败: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 从通用响应中提取content
     */
    private String parseContentFromResponse(String response) {
        try {
            JsonNode root = objectMapper.readTree(response);
            JsonNode choices = root.get("choices");
            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode message = choices.get(0).get("message");
                if (message != null && message.has("content")) {
                    return message.get("content").asText().trim();
                }
            }
        } catch (Exception e) {
            log.error("解析响应content失败: {}", e.getMessage());
        }
        return null;
    }
}
