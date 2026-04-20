package com.elderlycare.service.elder.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.dto.elder.HealthReportGenerateDTO;
import com.elderlycare.entity.elder.Elder;
import com.elderlycare.entity.elder.ElderHealth;
import com.elderlycare.entity.elder.HealthMeasurement;
import com.elderlycare.entity.elder.HealthReport;
import com.elderlycare.entity.servicelog.ServiceLog;
import com.elderlycare.mapper.elder.ElderHealthMapper;
import com.elderlycare.mapper.elder.ElderMapper;
import com.elderlycare.mapper.elder.HealthMeasurementMapper;
import com.elderlycare.mapper.elder.HealthReportMapper;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.service.elder.HealthReportService;
import com.elderlycare.vo.elder.HealthReportVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 健康报告Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HealthReportServiceImpl implements HealthReportService {

    private final HealthReportMapper reportMapper;
    private final ElderMapper elderMapper;
    private final ElderHealthMapper elderHealthMapper;
    private final HealthMeasurementMapper measurementMapper;
    private final ServiceLogMapper serviceLogMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public HealthReport generateReport(HealthReportGenerateDTO dto) {
        Elder elder = elderMapper.selectById(dto.getElderId());
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        // 查询老人健康档案
        ElderHealth health = getElderHealth(dto.getElderId());

        // 查询时间段内的服务记录
        List<ServiceLog> serviceLogs = getServiceLogs(dto.getElderId(), dto.getStartDate(), dto.getEndDate());

        // 查询时间段内的健康测量记录
        List<HealthMeasurement> measurements = getMeasurements(dto.getElderId(), dto.getStartDate(), dto.getEndDate());

        // 生成报告内容（JSON格式）
        Map<String, Object> reportContent = generateReportContent(elder, health, serviceLogs, measurements, dto);

        // 保存报告
        HealthReport report = new HealthReport();
        report.setReportId(IDGenerator.generateId());
        report.setElderId(dto.getElderId());
        report.setReportNo(generateReportNo(dto.getReportType()));
        report.setReportDate(dto.getEndDate() != null ? dto.getEndDate() : LocalDate.now());
        report.setReportType(dto.getReportType());
        report.setTitle(dto.getTitle() != null ? dto.getTitle() : generateTitle(dto.getReportType(), dto.getStartDate(), dto.getEndDate()));
        try {
            report.setContent(objectMapper.writeValueAsString(reportContent));
        } catch (Exception e) {
            log.error("Failed to serialize report content", e);
            throw new BusinessException(500, "报告内容序列化失败");
        }
        report.setCreateTime(LocalDateTime.now());
        reportMapper.insert(report);

        return report;
    }

    @Override
    public List<HealthReportVO> getReportList(String elderId) {
        LambdaQueryWrapper<HealthReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthReport::getElderId, elderId);
        wrapper.orderByDesc(HealthReport::getCreateTime);

        List<HealthReport> reports = reportMapper.selectList(wrapper);
        return reports.stream().map(this::convertToVO).collect(java.util.stream.Collectors.toList());
    }

    @Override
    public HealthReportVO getReportById(String reportId) {
        HealthReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(404, "报告不存在");
        }
        return convertToVO(report);
    }

    @Override
    public byte[] downloadPdf(String reportId) {
        HealthReport report = reportMapper.selectById(reportId);
        if (report == null) {
            throw new BusinessException(404, "报告不存在");
        }

        Elder elder = elderMapper.selectById(report.getElderId());
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        try {
            return generatePdfContent(report, elder);
        } catch (Exception e) {
            log.error("Failed to generate PDF", e);
            throw new BusinessException(500, "PDF生成失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteReport(String reportId) {
        reportMapper.deleteById(reportId);
    }

    private ElderHealth getElderHealth(String elderId) {
        try {
            LambdaQueryWrapper<ElderHealth> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ElderHealth::getElderId, elderId);
            return elderHealthMapper.selectOne(wrapper);
        } catch (Exception e) {
            log.warn("Failed to query elder health, skipping: {}", e.getMessage());
            return null;
        }
    }

    private List<ServiceLog> getServiceLogs(String elderId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<ServiceLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceLog::getElderId, elderId);
        if (startDate != null) {
            wrapper.ge(ServiceLog::getServiceDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(ServiceLog::getServiceDate, endDate);
        }
        wrapper.orderByDesc(ServiceLog::getServiceDate);
        wrapper.last("LIMIT 100");
        return serviceLogMapper.selectList(wrapper);
    }

    private List<HealthMeasurement> getMeasurements(String elderId, LocalDate startDate, LocalDate endDate) {
        if (startDate == null && endDate == null) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<HealthMeasurement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthMeasurement::getElderId, elderId);
        if (startDate != null) {
            wrapper.ge(HealthMeasurement::getMeasuredAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            wrapper.le(HealthMeasurement::getMeasuredAt, endDate.atTime(23, 59, 59));
        }
        wrapper.orderByDesc(HealthMeasurement::getMeasuredAt);
        wrapper.last("LIMIT 100");
        return measurementMapper.selectList(wrapper);
    }

    private Map<String, Object> generateReportContent(Elder elder, ElderHealth health,
                                                       List<ServiceLog> serviceLogs,
                                                       List<HealthMeasurement> measurements,
                                                       HealthReportGenerateDTO dto) {
        Map<String, Object> content = new LinkedHashMap<>();

        // 1. 老人基本信息
        Map<String, Object> elderInfo = new LinkedHashMap<>();
        elderInfo.put("elderId", elder.getElderId());
        elderInfo.put("name", elder.getName());
        elderInfo.put("gender", elder.getGender());
        elderInfo.put("age", elder.getAge());
        elderInfo.put("idCard", elder.getIdCard());
        elderInfo.put("phone", elder.getPhone());
        elderInfo.put("address", elder.getAddress());
        elderInfo.put("careType", elder.getCareType());
        elderInfo.put("careLevel", elder.getCareLevel());
        content.put("elderInfo", elderInfo);

        // 2. 健康档案摘要
        if (health != null) {
            Map<String, Object> healthInfo = new LinkedHashMap<>();
            healthInfo.put("bloodType", health.getBloodType());
            healthInfo.put("height", health.getHeight());
            healthInfo.put("weight", health.getWeight());
            healthInfo.put("medicalHistory", health.getMedicalHistory());
            healthInfo.put("currentMedication", health.getCurrentMedication());
            healthInfo.put("allergyHistory", health.getAllergyHistory());
            healthInfo.put("chronicDiseases", health.getChronicDiseases());
            healthInfo.put("adlScore", health.getAdlScore());
            healthInfo.put("mmseScore", health.getMmseScore());
            healthInfo.put("gdsScore", health.getGdsScore());
            healthInfo.put("fallRisk", health.getFallRisk());
            healthInfo.put("pressureSoreRisk", health.getPressureSoreRisk());
            healthInfo.put("nutritionStatus", health.getNutritionStatus());
            healthInfo.put("visionStatus", health.getVisionStatus());
            healthInfo.put("hearingStatus", health.getHearingStatus());
            healthInfo.put("oralHealth", health.getOralHealth());
            healthInfo.put("mobilityStatus", health.getMobilityStatus());
            content.put("healthInfo", healthInfo);
        }

        // 3. 服务统计
        Map<String, Object> serviceStats = new LinkedHashMap<>();
        serviceStats.put("totalCount", serviceLogs.size());

        // 按服务类型分组统计
        Map<String, Long> serviceTypeCount = new LinkedHashMap<>();
        for (ServiceLog log : serviceLogs) {
            String serviceType = log.getServiceTypeCode() != null ? log.getServiceTypeCode() : "UNKNOWN";
            serviceTypeCount.merge(serviceType, 1L, Long::sum);
        }
        serviceStats.put("serviceTypeCount", serviceTypeCount);
        content.put("serviceStats", serviceStats);

        // 4. 健康测量统计
        Map<String, Object> measurementStats = new LinkedHashMap<>();
        Map<String, List<String>> measurementsByType = new LinkedHashMap<>();
        for (HealthMeasurement m : measurements) {
            measurementsByType.computeIfAbsent(m.getMeasurementType(), k -> new ArrayList<>()).add(m.getMeasurementValue());
        }
        measurementStats.put("totalCount", measurements.size());
        measurementStats.put("measurementsByType", measurementsByType);
        content.put("measurementStats", measurementStats);

        // 5. 报告信息
        Map<String, Object> reportInfo = new LinkedHashMap<>();
        reportInfo.put("reportType", dto.getReportType());
        reportInfo.put("startDate", dto.getStartDate() != null ? dto.getStartDate().toString() : null);
        reportInfo.put("endDate", dto.getEndDate() != null ? dto.getEndDate().toString() : null);
        reportInfo.put("generateTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        content.put("reportInfo", reportInfo);

        return content;
    }

    private String generateReportNo(String reportType) {
        String prefix = switch (reportType) {
            case "MONTHLY" -> "MR";
            case "QUARTERLY" -> "QR";
            case "YEARLY" -> "YR";
            default -> "SR";
        };
        return prefix + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-" + System.currentTimeMillis() % 10000;
    }

    private String generateTitle(String reportType, LocalDate startDate, LocalDate endDate) {
        String typeName = switch (reportType) {
            case "MONTHLY" -> "月度";
            case "QUARTERLY" -> "季度";
            case "YEARLY" -> "年度";
            default -> "专项";
        };
        if (startDate != null && endDate != null) {
            return startDate + " 至 " + endDate + typeName + "健康报告";
        }
        return typeName + "健康报告";
    }

    private HealthReportVO convertToVO(HealthReport report) {
        HealthReportVO vo = new HealthReportVO();
        BeanUtils.copyProperties(report, vo);

        // 获取老人姓名
        Elder elder = elderMapper.selectById(report.getElderId());
        if (elder != null) {
            vo.setElderName(elder.getName());
        }

        // 报告类型名称
        vo.setReportTypeName(getReportTypeName(report.getReportType()));

        return vo;
    }

    private String getReportTypeName(String reportType) {
        if (reportType == null) return "";
        return switch (reportType) {
            case "MONTHLY" -> "月度报告";
            case "QUARTERLY" -> "季度报告";
            case "YEARLY" -> "年度报告";
            case "SPECIAL" -> "专项报告";
            default -> reportType;
        };
    }

    private String getReportTypeNameEn(String reportType) {
        if (reportType == null) return "";
        return switch (reportType) {
            case "MONTHLY" -> "Monthly Report";
            case "QUARTERLY" -> "Quarterly Report";
            case "YEARLY" -> "Yearly Report";
            case "SPECIAL" -> "Special Report";
            default -> reportType;
        };
    }

    private String getBloodTypeName(Integer bloodType) {
        if (bloodType == null) return "";
        return switch (bloodType) {
            case 0 -> "A型";
            case 1 -> "B型";
            case 2 -> "O型";
            case 3 -> "AB型";
            default -> "未知";
        };
    }

    private String getBloodTypeNameEn(Integer bloodType) {
        if (bloodType == null) return "";
        return switch (bloodType) {
            case 0 -> "A";
            case 1 -> "B";
            case 2 -> "O";
            case 3 -> "AB";
            default -> "Unknown";
        };
    }

    private String getFallRiskName(Integer fallRisk) {
        if (fallRisk == null) return "";
        return switch (fallRisk) {
            case 0 -> "低风险";
            case 1 -> "中风险";
            case 2 -> "高风险";
            default -> "未知";
        };
    }

    private String getFallRiskNameEn(Integer fallRisk) {
        if (fallRisk == null) return "";
        return switch (fallRisk) {
            case 0 -> "Low";
            case 1 -> "Medium";
            case 2 -> "High";
            default -> "Unknown";
        };
    }

    private byte[] generatePdfContent(HealthReport report, Elder elder) throws Exception {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                float margin = 50;
                float yStart = page.getMediaBox().getHeight() - margin;
                float y = yStart;
                float leading = 14.5f;
                float fontSize = 12;
                float titleSize = 18;

                // 使用支持中文的字体
                PDType1Font regularFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                PDType1Font boldFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                // 辅助方法：检测是否包含中文字符
                java.util.function.Predicate<String> containsChinese = (text) -> {
                    if (text == null || text.isEmpty()) return false;
                    for (char c : text.toCharArray()) {
                        if (Character.UnicodeBlock.of(c) == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS) {
                            return true;
                        }
                    }
                    return false;
                };

                float pageWidth = page.getMediaBox().getWidth() - 2 * margin;

                // 标题
                contentStream.beginText();
                contentStream.setFont(boldFont, titleSize);
                contentStream.newLineAtOffset(margin, y);
                String title = report.getTitle() != null ? report.getTitle() : "Health Report";
                // 如果标题包含中文，用英文替代
                contentStream.showText(containsChinese.test(title) ? "Health Report" : title);
                contentStream.endText();
                y -= titleSize * 2;

                // 报告信息
                drawText(contentStream, regularFont, fontSize, margin, y,
                    "Report No: " + (report.getReportNo() != null ? report.getReportNo() : "N/A"));
                y -= leading;

                drawText(contentStream, regularFont, fontSize, margin, y,
                    "Report Date: " + (report.getReportDate() != null ? report.getReportDate().toString() : "N/A"));
                y -= leading;

                drawText(contentStream, regularFont, fontSize, margin, y,
                    "Report Type: " + getReportTypeNameEn(report.getReportType()));
                y -= leading * 2;

                // 分割线
                contentStream.setLineWidth(1f);
                contentStream.moveTo(margin, y);
                contentStream.lineTo(page.getMediaBox().getWidth() - margin, y);
                contentStream.stroke();
                y -= leading * 2;

                // 老人基本信息
                contentStream.beginText();
                contentStream.setFont(boldFont, fontSize + 2);
                contentStream.newLineAtOffset(margin, y);
                contentStream.showText("1. Basic Information");
                contentStream.endText();
                y -= leading * 1.5f;

                // 姓名（可能是中文，显示拼音或英文替代）
                String name = elder.getName() != null ? stripNonAscii(elder.getName()) : "N/A";
                if (name.isEmpty()) name = "[Chinese Name]";
                drawText(contentStream, regularFont, fontSize, margin, y, "Name: " + name);
                y -= leading;

                drawText(contentStream, regularFont, fontSize, margin, y,
                    "Gender: " + (elder.getGender() != null ? elder.getGender() : "N/A"));
                y -= leading;

                drawText(contentStream, regularFont, fontSize, margin, y,
                    "Age: " + (elder.getAge() != null ? elder.getAge().toString() : "N/A"));
                y -= leading;

                drawText(contentStream, regularFont, fontSize, margin, y,
                    "ID Card: " + (elder.getIdCard() != null ? elder.getIdCard() : "N/A"));
                y -= leading;

                drawText(contentStream, regularFont, fontSize, margin, y,
                    "Phone: " + (elder.getPhone() != null ? elder.getPhone() : "N/A"));
                y -= leading * 2;

                // 解析并显示报告内容
                if (report.getContent() != null) {
                    try {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> content = objectMapper.readValue(report.getContent(), Map.class);

                        // 健康档案信息
                        if (content.containsKey("healthInfo")) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> healthInfo = (Map<String, Object>) content.get("healthInfo");
                            if (healthInfo != null && !healthInfo.isEmpty()) {
                                contentStream.beginText();
                                contentStream.setFont(boldFont, fontSize + 2);
                                contentStream.newLineAtOffset(margin, y);
                                contentStream.showText("2. Health Record");
                                contentStream.endText();
                                y -= leading * 1.5f;

                                if (healthInfo.get("bloodType") != null) {
                                    drawText(contentStream, regularFont, fontSize, margin, y,
                                        "Blood Type: " + getBloodTypeNameEn((Integer) healthInfo.get("bloodType")));
                                    y -= leading;
                                }

                                if (healthInfo.get("height") != null || healthInfo.get("weight") != null) {
                                    drawText(contentStream, regularFont, fontSize, margin, y,
                                        "Height/Weight: " +
                                        (healthInfo.get("height") != null ? healthInfo.get("height") + "cm" : "") + " / " +
                                        (healthInfo.get("weight") != null ? healthInfo.get("weight") + "kg" : ""));
                                    y -= leading;
                                }

                                if (healthInfo.get("adlScore") != null) {
                                    drawText(contentStream, regularFont, fontSize, margin, y,
                                        "ADL Score: " + healthInfo.get("adlScore"));
                                    y -= leading;
                                }

                                if (healthInfo.get("mmseScore") != null) {
                                    drawText(contentStream, regularFont, fontSize, margin, y,
                                        "MMSE Score: " + healthInfo.get("mmseScore"));
                                    y -= leading;
                                }

                                if (healthInfo.get("fallRisk") != null) {
                                    drawText(contentStream, regularFont, fontSize, margin, y,
                                        "Fall Risk: " + getFallRiskNameEn((Integer) healthInfo.get("fallRisk")));
                                    y -= leading;
                                }

                                y -= leading;
                            }
                        }

                        // 服务统计
                        if (content.containsKey("serviceStats")) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> serviceStats = (Map<String, Object>) content.get("serviceStats");
                            if (serviceStats != null) {
                                contentStream.beginText();
                                contentStream.setFont(boldFont, fontSize + 2);
                                contentStream.newLineAtOffset(margin, y);
                                contentStream.showText("3. Service Statistics");
                                contentStream.endText();
                                y -= leading * 1.5f;

                                drawText(contentStream, regularFont, fontSize, margin, y,
                                    "Total Services: " + serviceStats.get("totalCount"));
                                y -= leading * 2;
                            }
                        }

                        // 健康测量统计
                        if (content.containsKey("measurementStats")) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> measurementStats = (Map<String, Object>) content.get("measurementStats");
                            if (measurementStats != null) {
                                contentStream.beginText();
                                contentStream.setFont(boldFont, fontSize + 2);
                                contentStream.newLineAtOffset(margin, y);
                                contentStream.showText("4. Health Measurement Statistics");
                                contentStream.endText();
                                y -= leading * 1.5f;

                                drawText(contentStream, regularFont, fontSize, margin, y,
                                    "Total Measurements: " + measurementStats.get("totalCount"));
                                y -= leading;
                            }
                        }

                    } catch (Exception e) {
                        log.error("Failed to parse report content for PDF", e);
                    }
                }

                // 页脚
                y = margin;
                drawText(contentStream, regularFont, fontSize - 2, margin, y,
                    "Generated: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

                drawText(contentStream, regularFont, fontSize - 2,
                    page.getMediaBox().getWidth() - margin - 100, y,
                    "Elderly Care Platform");
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            document.save(baos);
            return baos.toByteArray();
        }
    }

    private void drawText(PDPageContentStream contentStream, PDType1Font font, float fontSize,
                          float x, float y, String text) throws java.io.IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        // 过滤非ASCII字符，防止Helvetica-Bold无法显示中文
        String safeText = stripNonAscii(text);
        contentStream.showText(safeText != null ? safeText : "");
        contentStream.endText();
    }

    // 移除非ASCII字符
    private String stripNonAscii(String text) {
        if (text == null || text.isEmpty()) return text;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c < 128) sb.append(c);
        }
        return sb.toString();
    }

}
