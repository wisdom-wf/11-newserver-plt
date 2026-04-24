package com.elderlycare.service.elder.impl;

import com.elderlycare.common.BusinessException;
import com.elderlycare.entity.elder.Elder;
import com.elderlycare.entity.elder.ElderHealth;
import com.elderlycare.entity.elder.HealthMeasurement;
import com.elderlycare.mapper.elder.ElderHealthMapper;
import com.elderlycare.mapper.elder.ElderMapper;
import com.elderlycare.mapper.elder.HealthMeasurementMapper;
import com.elderlycare.service.elder.HealthAdviceService;
import com.elderlycare.vo.elder.CareSuggestionVO;
import com.elderlycare.vo.elder.CareSuggestionVO.CareSuggestion;
import com.elderlycare.vo.elder.MedicalSuggestionVO;
import com.elderlycare.vo.elder.MedicalSuggestionVO.MedicalSuggestion;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 健康建议服务实现（基于规则引擎）
 */
@Service
@RequiredArgsConstructor
public class HealthAdviceServiceImpl implements HealthAdviceService {

    private final ElderMapper elderMapper;
    private final ElderHealthMapper elderHealthMapper;
    private final HealthMeasurementMapper measurementMapper;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public CareSuggestionVO getCareSuggestions(String elderId) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        CareSuggestionVO vo = new CareSuggestionVO();
        vo.setElderId(elderId);
        vo.setElderName(elder.getName());
        vo.setEvaluateTime(LocalDateTime.now().format(FORMATTER));
        vo.setSuggestions(new ArrayList<>());
        vo.setRiskAlerts(new ArrayList<>());

        // 获取健康档案
        ElderHealth health = elderHealthMapper.selectByElderId(elderId);
        Map<String, Object> healthData = buildHealthData(health);

        // 获取最近测量数据
        List<HealthMeasurement> recentMeasurements = getRecentMeasurements(elderId, 10);

        // 基于规则生成护理建议
        generateCareSuggestions(vo, elder, health, recentMeasurements, healthData);

        // 确定护理等级建议
        determineCareLevel(vo, elder, recentMeasurements, healthData);

        return vo;
    }

    @Override
    public MedicalSuggestionVO getMedicalSuggestions(String elderId) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        MedicalSuggestionVO vo = new MedicalSuggestionVO();
        vo.setElderId(elderId);
        vo.setElderName(elder.getName());
        vo.setEvaluateTime(LocalDateTime.now().format(FORMATTER));
        vo.setSuggestions(new ArrayList<>());
        vo.setSymptoms(new ArrayList<>());

        // 获取健康档案
        ElderHealth health = elderHealthMapper.selectByElderId(elderId);
        Map<String, Object> healthData = buildHealthData(health);

        // 获取最近测量数据
        List<HealthMeasurement> recentMeasurements = getRecentMeasurements(elderId, 10);

        // 基于规则生成就医建议
        generateMedicalSuggestions(vo, elder, health, recentMeasurements, healthData);

        return vo;
    }

    /**
     * 构建健康数据Map
     */
    private Map<String, Object> buildHealthData(ElderHealth health) {
        Map<String, Object> data = new HashMap<>();
        if (health != null) {
            data.put("bloodType", health.getBloodType());
            data.put("height", health.getHeight());
            data.put("weight", health.getWeight());
            data.put("medicalHistory", health.getMedicalHistory());
            data.put("allergyHistory", health.getAllergyHistory());
            data.put("currentMedication", health.getCurrentMedication());
            data.put("chronicDiseases", health.getChronicDiseases());
            data.put("adlScore", health.getAdlScore());
            data.put("mmseScore", health.getMmseScore());
            data.put("gdsScore", health.getGdsScore());
            data.put("fallRisk", health.getFallRisk());
        }
        return data;
    }

    /**
     * 获取最近的测量记录
     */
    private List<HealthMeasurement> getRecentMeasurements(String elderId, int limit) {
        return measurementMapper.selectByElderIdWithLimit(elderId, limit);
    }

    /**
     * 生成护理建议
     */
    private void generateCareSuggestions(CareSuggestionVO vo, Elder elder, ElderHealth health,
                                        List<HealthMeasurement> measurements, Map<String, Object> healthData) {
        List<CareSuggestion> suggestions = vo.getSuggestions();
        List<String> riskAlerts = vo.getRiskAlerts();

        // 分析血压趋势
        analyzeBloodPressure(suggestions, riskAlerts, measurements);

        // 分析血糖趋势
        analyzeBloodGlucose(suggestions, riskAlerts, measurements);

        // 分析体重变化
        analyzeWeight(suggestions, health);

        // 分析ADL评分
        analyzeADL(suggestions, healthData);

        // 分析认知功能
        analyzeMMSE(suggestions, healthData);

        // 分析跌倒风险
        analyzeFallRisk(suggestions, riskAlerts, healthData);

        // 分析用药情况
        analyzeMedication(suggestions, healthData);

        // 按优先级排序
        suggestions.sort(Comparator.comparingInt(CareSuggestion::getPriority).reversed());
    }

    private void analyzeBloodPressure(List<CareSuggestion> suggestions, List<String> riskAlerts,
                                     List<HealthMeasurement> measurements) {
        List<HealthMeasurement> bpMeasurements = measurements.stream()
                .filter(m -> "BLOOD_PRESSURE".equals(m.getMeasurementType()))
                .sorted(Comparator.comparing(HealthMeasurement::getMeasuredAt).reversed())
                .toList();

        if (bpMeasurements.isEmpty()) return;

        int highCount = 0;
        int alertCount = 0;
        String latestValue = null;

        for (HealthMeasurement m : bpMeasurements) {
            String[] parts = m.getMeasurementValue().split("/");
            if (parts.length == 2) {
                try {
                    int systolic = Integer.parseInt(parts[0].trim());
                    int diastolic = Integer.parseInt(parts[1].trim());
                    latestValue = m.getMeasurementValue();

                    if (systolic > 160 || diastolic > 100) {
                        alertCount++;
                        highCount++;
                    } else if (systolic > 140 || diastolic > 90) {
                        highCount++;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (alertCount >= 2) {
            riskAlerts.add("收缩压持续偏高（连续" + alertCount + "次 > 160/100mmHg），存在心血管风险");
            suggestions.add(createSuggestion("BLOOD_PRESSURE", "血压管理", "连续多次血压过高，建议密切监测血压变化，记录每日血压数据，如持续偏高应及时就医调整用药。", 1, "近期" + alertCount + "次血压测量 > 160/100mmHg"));
        } else if (highCount >= 3) {
            riskAlerts.add("血压偏高（连续" + highCount + "次 > 140/90mmHg）");
            suggestions.add(createSuggestion("BLOOD_PRESSURE", "血压管理", "血压偏高，建议减少盐分摄入，保持低脂饮食，适当增加户外散步等轻度运动。", 2, "近期" + highCount + "次血压测量 > 140/90mmHg"));
        } else if (highCount >= 1) {
            suggestions.add(createSuggestion("BLOOD_PRESSURE", "血压监测", "偶尔出现血压偏高，建议持续监测血压变化，注意饮食和情绪管理。", 3, "近期有血压偏高记录"));
        }
    }

    private void analyzeBloodGlucose(List<CareSuggestion> suggestions, List<String> riskAlerts,
                                    List<HealthMeasurement> measurements) {
        List<HealthMeasurement> bgMeasurements = measurements.stream()
                .filter(m -> "BLOOD_GLUCOSE".equals(m.getMeasurementType()))
                .sorted(Comparator.comparing(HealthMeasurement::getMeasuredAt).reversed())
                .toList();

        if (bgMeasurements.isEmpty()) return;

        int highCount = 0;
        int alertCount = 0;

        for (HealthMeasurement m : bgMeasurements) {
            try {
                BigDecimal value = new BigDecimal(m.getMeasurementValue().trim());
                if (value.compareTo(new BigDecimal("11.0")) > 0) {
                    alertCount++;
                    highCount++;
                } else if (value.compareTo(new BigDecimal("7.0")) > 0) {
                    highCount++;
                }
            } catch (NumberFormatException ignored) {}
        }

        if (alertCount >= 2) {
            riskAlerts.add("血糖持续过高（连续" + alertCount + "次 > 11.0mmol/L），需警惕糖尿病并发症");
            suggestions.add(createSuggestion("BLOOD_GLUCOSE", "血糖控制", "连续多次血糖过高，需警惕糖尿病，建议尽快就医复查血糖，调整饮食方案。", 1, "近期" + alertCount + "次血糖 > 11.0mmol/L"));
        } else if (highCount >= 3) {
            riskAlerts.add("血糖偏高（连续" + highCount + "次 > 7.0mmol/L）");
            suggestions.add(createSuggestion("BLOOD_GLUCOSE", "血糖管理", "血糖偏高，建议减少甜食摄入，主食定量，适当增加运动量。", 2, "近期" + highCount + "次血糖 > 7.0mmol/L"));
        } else if (highCount >= 1) {
            suggestions.add(createSuggestion("BLOOD_GLUCOSE", "血糖监测", "偶尔血糖偏高，建议关注餐后血糖变化。", 3, "近期有血糖偏高记录"));
        }
    }

    private void analyzeWeight(List<CareSuggestion> suggestions, ElderHealth health) {
        if (health == null) return;

        Double weight = health.getWeight();
        Double height = health.getHeight();

        if (weight != null && height != null && height > 0) {
            double bmi = weight / ((height / 100) * (height / 100));

            if (bmi < 18.5) {
                suggestions.add(createSuggestion("WEIGHT", "营养补充", "体重偏低（BMI=" + String.format("%.1f", bmi) + "），建议增加营养摄入，适当补充蛋白质。", 2, "BMI < 18.5"));
            } else if (bmi >= 28) {
                suggestions.add(createSuggestion("WEIGHT", "体重管理", "体重超标（BMI=" + String.format("%.1f", bmi) + "），建议控制饮食，适量增加运动。", 2, "BMI >= 28"));
            }
        }
    }

    private void analyzeADL(List<CareSuggestion> suggestions, Map<String, Object> healthData) {
        Integer adlScore = (Integer) healthData.get("adlScore");

        if (adlScore != null) {
            if (adlScore < 40) {
                suggestions.add(createSuggestion("ADL", "日常生活能力", "ADL评分偏低（" + adlScore + "分），日常生活需要较多协助，建议增加护理等级或申请专业护理服务。", 1, "ADL < 40分"));
            } else if (adlScore < 60) {
                suggestions.add(createSuggestion("ADL", "日常生活能力", "ADL评分偏低（" + adlScore + "分），建议进行康复训练，提高生活自理能力。", 2, "40 <= ADL < 60分"));
            }
        }
    }

    private void analyzeMMSE(List<CareSuggestion> suggestions, Map<String, Object> healthData) {
        Integer mmseScore = (Integer) healthData.get("mmseScore");

        if (mmseScore != null) {
            if (mmseScore < 18) {
                suggestions.add(createSuggestion("MMSE", "认知功能", "MMSE评分偏低（" + mmseScore + "分），存在认知障碍风险，建议就医评估，可考虑认知训练。", 1, "MMSE < 18分"));
            } else if (mmseScore < 24) {
                suggestions.add(createSuggestion("MMSE", "认知功能", "MMSE评分轻度下降（" + mmseScore + "分），建议进行认知功能训练，如记忆游戏、阅读等。", 2, "18 <= MMSE < 24分"));
            }
        }
    }

    private void analyzeFallRisk(List<CareSuggestion> suggestions, List<String> riskAlerts, Map<String, Object> healthData) {
        Integer fallRisk = (Integer) healthData.get("fallRisk");

        // fallRisk: 0-低风险，1-中风险，2-高风险
        if (fallRisk != null) {
            if (fallRisk == 2) {
                riskAlerts.add("跌倒风险评估为高风险");
                suggestions.add(createSuggestion("FALL_RISK", "跌倒预防", "跌倒风险高，需采取防跌倒措施：保持地面干燥，安装扶手，避免独自外出。", 1, "跌倒风险 = 高风险"));
            } else if (fallRisk == 1) {
                suggestions.add(createSuggestion("FALL_RISK", "跌倒预防", "跌倒风险中等，建议加强防跌倒意识，适量进行平衡训练。", 2, "跌倒风险 = 中风险"));
            }
        }
    }

    private void analyzeMedication(List<CareSuggestion> suggestions, Map<String, Object> healthData) {
        String medications = (String) healthData.get("currentMedication");

        if (medications == null || medications.isEmpty()) {
            suggestions.add(createSuggestion("MEDICATION", "用药管理", "暂无用药记录在案，如有用药请及时更新健康档案。", 4, "无用药记录"));
        }
    }

    private void determineCareLevel(CareSuggestionVO vo, Elder elder, List<HealthMeasurement> measurements,
                                    Map<String, Object> healthData) {
        String currentLevel = elder.getCareLevel();
        Integer adlScore = (Integer) healthData.get("adlScore");
        Integer mmseScore = (Integer) healthData.get("mmseScore");

        boolean needUpgrade = false;
        String reason = "";

        // ADL低于40需要高级别护理
        if (adlScore != null && adlScore < 40) {
            needUpgrade = true;
            reason += "ADL评分偏低；";
        }

        // MMSE低于18需要高级别护理
        if (mmseScore != null && mmseScore < 18) {
            needUpgrade = true;
            reason += "认知功能较差；";
        }

        if (needUpgrade) {
            vo.setCareLevelSuggestion("建议提升护理等级至高护理（" + reason + "）");
        } else if ("HIGH".equals(currentLevel)) {
            vo.setCareLevelSuggestion("当前护理等级合适，维持高级别护理");
        } else {
            vo.setCareLevelSuggestion("当前护理等级基本合适，可根据实际情况调整");
        }
    }

    /**
     * 生成就医建议
     */
    private void generateMedicalSuggestions(MedicalSuggestionVO vo, Elder elder, ElderHealth health,
                                          List<HealthMeasurement> measurements, Map<String, Object> healthData) {
        List<MedicalSuggestion> suggestions = vo.getSuggestions();
        List<String> symptoms = vo.getSymptoms();

        String urgencyLevel = "NORMAL";
        String urgencyLevelName = "一般关注";
        String suggestedDepartment = "内科";

        // 分析血压 - 确定就医紧急程度
        int bpAlert = analyzeBpForMedical(measurements, symptoms);
        if (bpAlert > urgencyLevelToInt(urgencyLevel)) {
            urgencyLevel = intToUrgencyLevel(bpAlert);
            suggestedDepartment = "心血管内科";
        }

        // 分析血糖
        int bgAlert = analyzeBgForMedical(measurements, symptoms);
        if (bgAlert > urgencyLevelToInt(urgencyLevel)) {
            urgencyLevel = intToUrgencyLevel(bgAlert);
            suggestedDepartment = "内分泌科";
        }

        // 分析血氧
        int spo2Alert = analyzeSpo2ForMedical(measurements, symptoms);
        if (spo2Alert > urgencyLevelToInt(urgencyLevel)) {
            urgencyLevel = intToUrgencyLevel(spo2Alert);
            suggestedDepartment = "呼吸内科";
        }

        // 根据既往病史建议科室
        String medicalHistory = (String) healthData.get("medicalHistory");
        if (medicalHistory != null) {
            if (medicalHistory.contains("心脏病") || medicalHistory.contains("冠心病")) {
                suggestedDepartment = "心血管内科";
            } else if (medicalHistory.contains("糖尿病")) {
                suggestedDepartment = "内分泌科";
            } else if (medicalHistory.contains("脑卒中") || medicalHistory.contains("中风")) {
                suggestedDepartment = "神经内科";
            }
        }

        // 生成建议
        if (bpAlert >= 2) {
            suggestions.add(createMedicalSuggestion("BLOOD_PRESSURE", "血压异常", "血压持续异常，建议心血管内科就诊，评估是否需要调整降压方案。", bpAlert, "血压连续偏高"));
        }

        if (bgAlert >= 2) {
            suggestions.add(createMedicalSuggestion("BLOOD_GLUCOSE", "血糖异常", "血糖控制不佳，建议内分泌科就诊，评估糖尿病用药方案。", bgAlert, "血糖持续偏高"));
        }

        if (spo2Alert >= 2) {
            suggestions.add(createMedicalSuggestion("SPO2", "血氧异常", "血氧偏低，建议呼吸内科就诊，排除肺部疾病。", spo2Alert, "血氧 < 95%"));
        }

        // 根据年龄和症状建议体检
        Integer age = elder.getAge();
        if (age != null && age >= 65) {
            suggestions.add(createMedicalSuggestion("CHECKUP", "定期体检", "年龄超过65岁，建议每年进行一次全面体检，包括心电图、血脂、血糖等。", 3, "年龄 >= 65岁"));
        }

        vo.setUrgencyLevel(urgencyLevel);
        vo.setUrgencyLevelName(getUrgencyLevelName(urgencyLevel));
        vo.setSuggestedDepartment(suggestedDepartment);

        // 按优先级排序
        suggestions.sort(Comparator.comparingInt(MedicalSuggestion::getPriority).reversed());
    }

    private int analyzeBpForMedical(List<HealthMeasurement> measurements, List<String> symptoms) {
        List<HealthMeasurement> bpMeasurements = measurements.stream()
                .filter(m -> "BLOOD_PRESSURE".equals(m.getMeasurementType()))
                .sorted(Comparator.comparing(HealthMeasurement::getMeasuredAt).reversed())
                .toList();

        if (bpMeasurements.isEmpty()) return 0;

        int alertCount = 0;
        int warningCount = 0;
        String latestValue = null;

        for (HealthMeasurement m : bpMeasurements) {
            String[] parts = m.getMeasurementValue().split("/");
            if (parts.length == 2) {
                try {
                    int systolic = Integer.parseInt(parts[0].trim());
                    int diastolic = Integer.parseInt(parts[1].trim());
                    latestValue = m.getMeasurementValue();

                    if (systolic > 180 || diastolic > 110) {
                        alertCount++;
                        symptoms.add("血压过高：" + m.getMeasurementValue());
                    } else if (systolic > 160 || diastolic > 100) {
                        warningCount++;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        if (alertCount >= 2) return 3; // URGENT
        if (alertCount >= 1) return 2; // WARNING
        if (warningCount >= 3) return 2;
        if (warningCount >= 1) return 1;
        return 0;
    }

    private int analyzeBgForMedical(List<HealthMeasurement> measurements, List<String> symptoms) {
        List<HealthMeasurement> bgMeasurements = measurements.stream()
                .filter(m -> "BLOOD_GLUCOSE".equals(m.getMeasurementType()))
                .sorted(Comparator.comparing(HealthMeasurement::getMeasuredAt).reversed())
                .toList();

        if (bgMeasurements.isEmpty()) return 0;

        int alertCount = 0;
        int warningCount = 0;

        for (HealthMeasurement m : bgMeasurements) {
            try {
                BigDecimal value = new BigDecimal(m.getMeasurementValue().trim());
                if (value.compareTo(new BigDecimal("13.9")) > 0) {
                    alertCount++;
                    symptoms.add("血糖过高：" + m.getMeasurementValue() + "mmol/L");
                } else if (value.compareTo(new BigDecimal("11.0")) > 0) {
                    warningCount++;
                }
            } catch (NumberFormatException ignored) {}
        }

        if (alertCount >= 2) return 3;
        if (alertCount >= 1) return 2;
        if (warningCount >= 3) return 2;
        if (warningCount >= 1) return 1;
        return 0;
    }

    private int analyzeSpo2ForMedical(List<HealthMeasurement> measurements, List<String> symptoms) {
        List<HealthMeasurement> spo2Measurements = measurements.stream()
                .filter(m -> "SPO2".equals(m.getMeasurementType()))
                .sorted(Comparator.comparing(HealthMeasurement::getMeasuredAt).reversed())
                .toList();

        if (spo2Measurements.isEmpty()) return 0;

        int alertCount = 0;

        for (HealthMeasurement m : spo2Measurements) {
            try {
                BigDecimal value = new BigDecimal(m.getMeasurementValue().trim());
                if (value.compareTo(new BigDecimal("90")) < 0) {
                    alertCount++;
                    symptoms.add("血氧过低：" + m.getMeasurementValue() + "%");
                }
            } catch (NumberFormatException ignored) {}
        }

        if (alertCount >= 1) return 3;
        return 0;
    }

    private int urgencyLevelToInt(String level) {
        switch (level) {
            case "URGENT": return 3;
            case "WARNING": return 2;
            case "ATTENTION": return 1;
            default: return 0;
        }
    }

    private String intToUrgencyLevel(int level) {
        switch (level) {
            case 3: return "URGENT";
            case 2: return "WARNING";
            case 1: return "ATTENTION";
            default: return "NORMAL";
        }
    }

    private String getUrgencyLevelName(String level) {
        switch (level) {
            case "URGENT": return "紧急就医";
            case "WARNING": return "尽快就医";
            case "ATTENTION": return "一般关注";
            default: return "正常";
        }
    }

    private CareSuggestion createSuggestion(String type, String typeName, String content, int priority, String basis) {
        CareSuggestion suggestion = new CareSuggestion();
        suggestion.setType(type);
        suggestion.setTypeName(typeName);
        suggestion.setContent(content);
        suggestion.setPriority(priority);
        suggestion.setBasis(basis);
        return suggestion;
    }

    private MedicalSuggestion createMedicalSuggestion(String type, String typeName, String content, int priority, String basis) {
        MedicalSuggestion suggestion = new MedicalSuggestion();
        suggestion.setType(type);
        suggestion.setTypeName(typeName);
        suggestion.setContent(content);
        suggestion.setPriority(priority);
        suggestion.setBasis(basis);
        return suggestion;
    }
}
