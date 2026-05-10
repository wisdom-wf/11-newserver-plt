package com.elderlycare.service.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

import java.util.*;

/**
 * 图片理解服务（OCR/内容识别）
 * 使用 qwen-vl-plus 识别老人上传的药品照片、化验单、测量读数等
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageUnderstandingService {

    private final AliyunAiService aliyunAiService;
    private final ObjectMapper objectMapper;

    /**
     * 识别药品照片，返回结构化信息
     * @param imageInput 图片URL或base64
     * @return 药品信息结构
     */
    public PrescriptionRecognitionResult recognizePrescription(String imageInput) {
        String prompt = """
            请仔细识别这张药品照片，提取以下信息并以JSON格式返回：
            {
              "drugName": "药品通用名称（如：硝苯地平缓释片）",
              "brandName": "品牌名（如有）",
              "dosage": "剂量（如：10mg）",
              "frequency": "用法用量（如：每日2次，每次1片）",
              "duration": "疗程（如：7天，或遵医嘱）",
              "route": "给药途径（如：口服）",
              "storageCondition": "储存条件（如：避光、密封保存）",
              "expireDate": "有效期（如能识别）",
              "manufacturer": "生产厂家（如能识别）",
              "rawText": "原始识别文本"
            }
            如果无法识别某字段，返回null。不要编造信息。
            """;

        String result = aliyunAiService.understandImage(imageInput, prompt);
        if (result == null) {
            return null;
        }

        try {
            // 去掉可能的markdown代码块
            String json = result.trim();
            if (json.startsWith("```json")) json = json.substring(7);
            else if (json.startsWith("```")) json = json.substring(3);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);

            JsonNode node = objectMapper.readTree(json);
            PrescriptionRecognitionResult r = new PrescriptionRecognitionResult();
            r.setDrugName(getText(node, "drugName"));
            r.setBrandName(getText(node, "brandName"));
            r.setDosage(getText(node, "dosage"));
            r.setFrequency(getText(node, "frequency"));
            r.setDuration(getText(node, "duration"));
            r.setRoute(getText(node, "route"));
            r.setStorageCondition(getText(node, "storageCondition"));
            r.setExpireDate(getText(node, "expireDate"));
            r.setManufacturer(getText(node, "manufacturer"));
            r.setRawText(getText(node, "rawText"));
            return r;
        } catch (Exception e) {
            log.error("解析药品识别结果失败: {}", e.getMessage());
            PrescriptionRecognitionResult r = new PrescriptionRecognitionResult();
            r.setRawText(result);
            return r;
        }
    }

    /**
     * 从图片识别血压/血糖等测量读数
     * @param imageInput 图片URL或base64
     * @param measurementType 测量类型（BLOOD_PRESSURE/BLOOD_GLUCOSE等）
     * @return 识别出的测量值
     */
    public String recognizeMeasurementValue(String imageInput, String measurementType) {
        String prompt = switch (measurementType) {
            case "BLOOD_PRESSURE" ->
                "这是一张血压计显示的照片，请识别其中的收缩压和舒张压数值，格式：收缩压/舒张压，单位mmHg，如：120/80。如果无法识别，返回空字符串。";
            case "BLOOD_GLUCOSE" ->
                "这是一张血糖仪显示的照片，请识别其中的血糖数值和单位（如mmol/L或mg/dL），返回格式：数值+单位，如：5.6mmol/L。如果无法识别，返回空字符串。";
            case "TEMPERATURE" ->
                "这是一张体温计显示的照片，请识别其中的体温数值和单位（如℃或°F），返回格式：数值+单位，如：36.5℃。如果无法识别，返回空字符串。";
            case "WEIGHT" ->
                "这是一张体重秤显示的照片，请识别人体重量的数值和单位（如kg或斤），返回格式：数值+单位，如：65kg。如果无法识别，返回空字符串。";
            default ->
                "请识别这张图片中的所有数字和文字，描述图片内容。";
        };

        String result = aliyunAiService.understandImage(imageInput, prompt);
        return result != null ? result.trim() : "";
    }

    /**
     * 识别化验单，返回关键指标
     * @param imageInput 图片URL或base64
     * @return 化验单解读
     */
    public LabReportResult recognizeLabReport(String imageInput) {
        String prompt = """
            请识别这张化验单或检查报告，提取以下信息并以JSON格式返回：
            {
              "reportType": "报告类型（如：血常规、尿常规、肝功能、肾功能、血脂、血糖等）",
              "testDate": "检测日期（如能识别）",
              "items": [
                {
                  "name": "指标名称（如：空腹血糖）",
                  "value": "检测值（如：5.6）",
                  "unit": "单位（如：mmol/L）",
                  "referenceRange": "参考区间（如：3.9-6.1）",
                  "flag": "异常标志（H-偏高、L-偏低，如有）"
                }
              ],
              "summary": "综合解读：简要说明有哪些指标异常及临床意义（50字以内）",
              "rawText": "原始识别文本"
            }
            如果无法识别某字段，返回null或空数组。不要编造信息。
            """;

        String result = aliyunAiService.understandImage(imageInput, prompt);
        if (result == null) {
            return null;
        }

        try {
            String json = result.trim();
            if (json.startsWith("```json")) json = json.substring(7);
            else if (json.startsWith("```")) json = json.substring(3);
            if (json.endsWith("```")) json = json.substring(0, json.length() - 3);

            JsonNode node = objectMapper.readTree(json);
            LabReportResult r = new LabReportResult();
            r.setReportType(getText(node, "reportType"));
            r.setTestDate(getText(node, "testDate"));
            r.setSummary(getText(node, "summary"));
            r.setRawText(getText(node, "rawText"));

            JsonNode items = node.get("items");
            if (items != null && items.isArray()) {
                List<LabItemResult> list = new ArrayList<>();
                for (JsonNode item : items) {
                    LabItemResult i = new LabItemResult();
                    i.setName(getText(item, "name"));
                    i.setValue(getText(item, "value"));
                    i.setUnit(getText(item, "unit"));
                    i.setReferenceRange(getText(item, "referenceRange"));
                    i.setFlag(getText(item, "flag"));
                    list.add(i);
                }
                r.setItems(list);
            }
            return r;
        } catch (Exception e) {
            log.error("解析化验单识别结果失败: {}", e.getMessage());
            LabReportResult r = new LabReportResult();
            r.setRawText(result);
            return r;
        }
    }

    private String getText(JsonNode node, String field) {
        JsonNode n = node.get(field);
        return n != null && !n.isNull() ? n.asText() : null;
    }

    // ==================== 结果内部类 ====================

    @lombok.Data
    public static class PrescriptionRecognitionResult {
        private String drugName;
        private String brandName;
        private String dosage;
        private String frequency;
        private String duration;
        private String route;
        private String storageCondition;
        private String expireDate;
        private String manufacturer;
        private String rawText;
    }

    @lombok.Data
    public static class LabReportResult {
        private String reportType;
        private String testDate;
        private List<LabItemResult> items;
        private String summary;
        private String rawText;
    }

    @lombok.Data
    public static class LabItemResult {
        private String name;
        private String value;
        private String unit;
        private String referenceRange;
        private String flag; // H/L 偏高偏低
    }
}
