package com.elderlycare.service.elder.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.dto.elder.HealthMeasurementDTO;
import com.elderlycare.entity.elder.Elder;
import com.elderlycare.entity.elder.HealthMeasurement;
import com.elderlycare.mapper.elder.ElderMapper;
import com.elderlycare.mapper.elder.HealthMeasurementMapper;
import com.elderlycare.service.elder.HealthMeasurementService;
import com.elderlycare.vo.elder.HealthMeasurementStatisticsVO;
import com.elderlycare.vo.elder.HealthMeasurementVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 健康测量记录Service实现类
 */
@Service
@RequiredArgsConstructor
public class HealthMeasurementServiceImpl implements HealthMeasurementService {

    private final HealthMeasurementMapper measurementMapper;
    private final ElderMapper elderMapper;

    /**
     * 测量类型映射
     */
    private static final Map<String, String> MEASUREMENT_TYPE_NAMES = Map.of(
            "BLOOD_PRESSURE", "血压",
            "BLOOD_GLUCOSE", "血糖",
            "WEIGHT", "体重",
            "TEMPERATURE", "体温",
            "PULSE", "脉搏",
            "SPO2", "血氧",
            "PAIN_SCALE", "疼痛指数",
            "OTHER", "其他"
    );

    /**
     * 测量类型单位
     */
    private static final Map<String, String> MEASUREMENT_TYPE_UNITS = Map.of(
            "BLOOD_PRESSURE", "mmHg",
            "BLOOD_GLUCOSE", "mmol/L",
            "WEIGHT", "kg",
            "TEMPERATURE", "°C",
            "PULSE", "bpm",
            "SPO2", "%",
            "PAIN_SCALE", "(0-10)"
    );

    @Override
    @Transactional
    public HealthMeasurement addMeasurement(String elderId, HealthMeasurementDTO dto) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        HealthMeasurement measurement = new HealthMeasurement();
        measurement.setMeasurementId(IDGenerator.generateId());
        measurement.setElderId(elderId);
        measurement.setServiceLogId(dto.getServiceLogId());
        measurement.setMeasurementType(dto.getMeasurementType());
        measurement.setMeasurementValue(dto.getMeasurementValue());
        measurement.setMeasurementUnit(getUnit(dto.getMeasurementType()));
        measurement.setMeasuredAt(dto.getMeasuredAt() != null ? dto.getMeasuredAt() : LocalDateTime.now());
        measurement.setRemark(dto.getRemark());
        measurement.setCreateTime(LocalDateTime.now());

        measurementMapper.insert(measurement);
        return measurement;
    }

    @Override
    @Transactional
    public List<HealthMeasurement> addMeasurements(String elderId, List<HealthMeasurementDTO> dtos) {
        Elder elder = elderMapper.selectById(elderId);
        if (elder == null) {
            throw new BusinessException(404, "老人档案不存在");
        }

        List<HealthMeasurement> measurements = new ArrayList<>();
        for (HealthMeasurementDTO dto : dtos) {
            HealthMeasurement measurement = new HealthMeasurement();
            measurement.setMeasurementId(IDGenerator.generateId());
            measurement.setElderId(elderId);
            measurement.setServiceLogId(dto.getServiceLogId());
            measurement.setMeasurementType(dto.getMeasurementType());
            measurement.setMeasurementValue(dto.getMeasurementValue());
            measurement.setMeasurementUnit(getUnit(dto.getMeasurementType()));
            measurement.setMeasuredAt(dto.getMeasuredAt() != null ? dto.getMeasuredAt() : LocalDateTime.now());
            measurement.setRemark(dto.getRemark());
            measurement.setCreateTime(LocalDateTime.now());
            measurementMapper.insert(measurement);
            measurements.add(measurement);
        }
        return measurements;
    }

    @Override
    public List<HealthMeasurementVO> getMeasurementHistory(String elderId, String measurementType, Integer limit) {
        LambdaQueryWrapper<HealthMeasurement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthMeasurement::getElderId, elderId);
        if (measurementType != null && !measurementType.isEmpty()) {
            wrapper.eq(HealthMeasurement::getMeasurementType, measurementType);
        }
        wrapper.orderByDesc(HealthMeasurement::getMeasuredAt);
        if (limit != null && limit > 0) {
            wrapper.last("LIMIT " + limit);
        }

        List<HealthMeasurement> measurements = measurementMapper.selectList(wrapper);
        return measurements.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public HealthMeasurementVO getLatestMeasurement(String elderId, String measurementType) {
        LambdaQueryWrapper<HealthMeasurement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthMeasurement::getElderId, elderId);
        if (measurementType != null && !measurementType.isEmpty()) {
            wrapper.eq(HealthMeasurement::getMeasurementType, measurementType);
        }
        wrapper.orderByDesc(HealthMeasurement::getMeasuredAt);
        wrapper.last("LIMIT 1");

        HealthMeasurement measurement = measurementMapper.selectOne(wrapper);
        return measurement != null ? convertToVO(measurement) : null;
    }

    @Override
    public List<HealthMeasurementVO> getLatestMeasurements(String elderId) {
        // 获取每种类型的最新测量记录
        LambdaQueryWrapper<HealthMeasurement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthMeasurement::getElderId, elderId);
        wrapper.orderByDesc(HealthMeasurement::getMeasuredAt);

        List<HealthMeasurement> measurements = measurementMapper.selectList(wrapper);

        // 按类型分组，每组取最新的
        Map<String, HealthMeasurement> latestByType = measurements.stream()
                .collect(Collectors.toMap(
                        HealthMeasurement::getMeasurementType,
                        m -> m,
                        (m1, m2) -> m1.getMeasuredAt().isAfter(m2.getMeasuredAt()) ? m1 : m2
                ));

        return latestByType.values().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public HealthMeasurementStatisticsVO getMeasurementStatistics(String elderId, String measurementType) {
        LambdaQueryWrapper<HealthMeasurement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthMeasurement::getElderId, elderId);
        if (measurementType != null && !measurementType.isEmpty()) {
            wrapper.eq(HealthMeasurement::getMeasurementType, measurementType);
        }
        wrapper.orderByDesc(HealthMeasurement::getMeasuredAt);
        wrapper.last("LIMIT 30"); // 最近30条

        List<HealthMeasurement> measurements = measurementMapper.selectList(wrapper);

        HealthMeasurementStatisticsVO stats = new HealthMeasurementStatisticsVO();
        stats.setMeasurementType(measurementType);
        stats.setMeasurementTypeName(getTypeName(measurementType));

        if (measurements.isEmpty()) {
            stats.setCount(0);
            return stats;
        }

        stats.setCount(measurements.size());

        // 最新值
        HealthMeasurement latest = measurements.get(0);
        stats.setLatestValue(latest.getMeasurementValue());
        stats.setLatestTime(latest.getMeasuredAt().toString());

        // 计算血压的平均值（如果是血压需要特殊处理收缩压/舒张压）
        if ("BLOOD_PRESSURE".equals(measurementType)) {
            List<Integer> systolicValues = new ArrayList<>();
            List<Integer> diastolicValues = new ArrayList<>();
            for (HealthMeasurement m : measurements) {
                String[] parts = m.getMeasurementValue().split("/");
                if (parts.length == 2) {
                    try {
                        systolicValues.add(Integer.parseInt(parts[0].trim()));
                        diastolicValues.add(Integer.parseInt(parts[1].trim()));
                    } catch (NumberFormatException ignored) {}
                }
            }
            if (!systolicValues.isEmpty()) {
                int systolicAvg = (int) systolicValues.stream().mapToInt(Integer::intValue).average().orElse(0);
                int diastolicAvg = (int) diastolicValues.stream().mapToInt(Integer::intValue).average().orElse(0);
                // 血压值用字符串存储，因为格式是 "收缩压/舒张压"
                stats.setAverageValue(systolicAvg + "/" + diastolicAvg);
                stats.setMaxValue(Collections.max(systolicValues) + "/" + Collections.max(diastolicValues));
                stats.setMinValue(Collections.min(systolicValues) + "/" + Collections.min(diastolicValues));
            }
        } else {
            List<BigDecimal> values = new ArrayList<>();
            for (HealthMeasurement m : measurements) {
                try {
                    values.add(new BigDecimal(m.getMeasurementValue().trim()));
                } catch (NumberFormatException ignored) {}
            }
            if (!values.isEmpty()) {
                BigDecimal sum = values.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
                stats.setAverageValue(sum.divide(new BigDecimal(values.size()), 1, RoundingMode.HALF_UP));
                stats.setMaxValue(Collections.max(values));
                stats.setMinValue(Collections.min(values));
            }
        }

        // 预警判断
        checkAlertStatus(stats, measurementType, measurements);

        // 趋势数据
        List<Map<String, Object>> trendData = measurements.stream()
                .limit(10)
                .map(m -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("value", m.getMeasurementValue());
                    item.put("time", m.getMeasuredAt().toString());
                    return item;
                })
                .collect(Collectors.toList());
        stats.setTrendData(trendData);

        return stats;
    }

    @Override
    public List<HealthMeasurementStatisticsVO> getAllMeasurementStatistics(String elderId) {
        // 获取所有测量类型
        LambdaQueryWrapper<HealthMeasurement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthMeasurement::getElderId, elderId);
        wrapper.select(HealthMeasurement::getMeasurementType);
        wrapper.groupBy(HealthMeasurement::getMeasurementType);

        List<HealthMeasurement> distinctTypes = measurementMapper.selectList(wrapper);

        return distinctTypes.stream()
                .map(m -> getMeasurementStatistics(elderId, m.getMeasurementType()))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteMeasurement(String measurementId) {
        measurementMapper.deleteById(measurementId);
    }

    /**
     * 转换为VO
     */
    private HealthMeasurementVO convertToVO(HealthMeasurement measurement) {
        HealthMeasurementVO vo = new HealthMeasurementVO();
        vo.setMeasurementId(measurement.getMeasurementId());
        vo.setElderId(measurement.getElderId());
        vo.setServiceLogId(measurement.getServiceLogId());
        vo.setMeasurementType(measurement.getMeasurementType());
        vo.setMeasurementTypeName(getTypeName(measurement.getMeasurementType()));
        vo.setMeasurementValue(measurement.getMeasurementValue());
        vo.setMeasurementUnit(measurement.getMeasurementUnit());
        vo.setMeasuredAt(measurement.getMeasuredAt());
        vo.setStaffId(measurement.getStaffId());
        vo.setStaffName(measurement.getStaffName());
        vo.setRemark(measurement.getRemark());
        vo.setCreateTime(measurement.getCreateTime());
        return vo;
    }

    /**
     * 获取类型名称
     */
    private String getTypeName(String type) {
        return MEASUREMENT_TYPE_NAMES.getOrDefault(type, type);
    }

    /**
     * 获取单位
     */
    private String getUnit(String type) {
        return MEASUREMENT_TYPE_UNITS.getOrDefault(type, "");
    }

    /**
     * 预警判断
     */
    private void checkAlertStatus(HealthMeasurementStatisticsVO stats, String measurementType, List<HealthMeasurement> measurements) {
        if (measurements.isEmpty()) {
            stats.setAlertStatus("NORMAL");
            return;
        }

        String alertStatus = "NORMAL";
        String alertMessage = "";

        switch (measurementType) {
            case "BLOOD_PRESSURE":
                // 连续3次血压 > 140/90 预警
                for (HealthMeasurement m : measurements) {
                    String[] parts = m.getMeasurementValue().split("/");
                    if (parts.length == 2) {
                        try {
                            int systolic = Integer.parseInt(parts[0].trim());
                            int diastolic = Integer.parseInt(parts[1].trim());
                            if (systolic > 140 || diastolic > 90) {
                                alertStatus = "WARNING";
                                alertMessage = "血压偏高，建议关注";
                                break;
                            }
                            if (systolic > 160 || diastolic > 100) {
                                alertStatus = "ALERT";
                                alertMessage = "血压过高，建议就医";
                                break;
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                break;
            case "BLOOD_GLUCOSE":
                // 血糖 > 7.0 预警
                for (HealthMeasurement m : measurements) {
                    try {
                        BigDecimal value = new BigDecimal(m.getMeasurementValue().trim());
                        if (value.compareTo(new BigDecimal("7.0")) > 0) {
                            alertStatus = "WARNING";
                            alertMessage = "血糖偏高，建议关注饮食";
                            break;
                        }
                        if (value.compareTo(new BigDecimal("11.0")) > 0) {
                            alertStatus = "ALERT";
                            alertMessage = "血糖过高，建议就医";
                            break;
                        }
                    } catch (NumberFormatException ignored) {}
                }
                break;
            case "SPO2":
                // 血氧 < 94% 预警
                for (HealthMeasurement m : measurements) {
                    try {
                        BigDecimal value = new BigDecimal(m.getMeasurementValue().trim());
                        if (value.compareTo(new BigDecimal("94")) < 0) {
                            alertStatus = "ALERT";
                            alertMessage = "血氧过低，建议就医";
                            break;
                        } else if (value.compareTo(new BigDecimal("96")) < 0) {
                            alertStatus = "WARNING";
                            alertMessage = "血氧偏低，建议关注";
                        }
                    } catch (NumberFormatException ignored) {}
                }
                break;
            default:
                break;
        }

        stats.setAlertStatus(alertStatus);
        stats.setAlertMessage(alertMessage);
    }

    @Override
    public int calculateHealthIndex(String elderId) {
        // 获取老人所有类型的最新测量
        List<HealthMeasurementVO> latestMeasurements = getLatestMeasurements(elderId);

        if (latestMeasurements == null || latestMeasurements.isEmpty()) {
            return 75; // 没有测量数据时返回中等分数
        }

        int totalScore = 60; // 基础分
        int count = 0;

        for (HealthMeasurementVO measurement : latestMeasurements) {
            int score = evaluateMeasurementScore(measurement.getMeasurementType(), measurement.getMeasurementValue());
            totalScore += score;
            count++;
        }

        // 考虑测量类型数量进行调整
        if (count > 0) {
            // 根据实际测量类型数量微调分数
            int averageExtra = (totalScore - 60) / count;
            totalScore = Math.min(100, Math.max(0, 60 + averageExtra * Math.min(count, 4)));
        }

        return totalScore;
    }

    /**
     * 根据测量类型和值计算加分
     * 满分100，基础分60，各类型正常加分
     */
    private int evaluateMeasurementScore(String type, String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }

        try {
            switch (type) {
                case "BLOOD_PRESSURE":
                    // 血压：正常 120/80 左右 +20，高血压 140/90 +10，超高 160/100 -10
                    String[] bpParts = value.split("/");
                    if (bpParts.length == 2) {
                        int systolic = Integer.parseInt(bpParts[0].trim());
                        int diastolic = Integer.parseInt(bpParts[1].trim());
                        if (systolic >= 90 && systolic <= 120 && diastolic >= 60 && diastolic <= 80) {
                            return 20; // 正常
                        } else if (systolic <= 140 && diastolic <= 90) {
                            return 10; // 正常高值
                        } else if (systolic > 160 || diastolic > 100) {
                            return -10; // 高血压
                        }
                        return 5; // 其他情况
                    }
                    return 0;

                case "BLOOD_GLUCOSE":
                    // 血糖：正常 4.4-6.1 +15，偏高 6.1-7.0 +5，超高 >7.0 -5
                    BigDecimal glucose = new BigDecimal(value.trim());
                    if (glucose.compareTo(new BigDecimal("4.4")) >= 0 && glucose.compareTo(new BigDecimal("6.1")) <= 0) {
                        return 15; // 正常
                    } else if (glucose.compareTo(new BigDecimal("7.0")) <= 0) {
                        return 5; // 偏高
                    } else if (glucose.compareTo(new BigDecimal("11.0")) > 0) {
                        return -5; // 超高
                    }
                    return 0;

                case "WEIGHT":
                    // 体重：根据BMI估算，正常范围 +10
                    // 这里简化处理，体重值在40-80kg之间视为正常
                    try {
                        double weight = Double.parseDouble(value.trim());
                        if (weight >= 40 && weight <= 80) {
                            return 10;
                        } else if (weight >= 30 && weight <= 100) {
                            return 5;
                        }
                    } catch (NumberFormatException ignored) {}
                    return 0;

                case "TEMPERATURE":
                    // 体温：36.3-37.2 正常 +10
                    BigDecimal temp = new BigDecimal(value.trim());
                    if (temp.compareTo(new BigDecimal("36.3")) >= 0 && temp.compareTo(new BigDecimal("37.2")) <= 0) {
                        return 10;
                    } else if (temp.compareTo(new BigDecimal("38.0")) > 0) {
                        return -5; // 发烧
                    }
                    return 5;

                case "PULSE":
                    // 脉搏：60-100 正常 +10
                    int pulse = Integer.parseInt(value.trim());
                    if (pulse >= 60 && pulse <= 100) {
                        return 10;
                    }
                    return 5;

                case "SPO2":
                    // 血氧：95-100 正常 +10，94 偏低，<94 预警
                    int spo2 = Integer.parseInt(value.trim());
                    if (spo2 >= 95) {
                        return 10;
                    } else if (spo2 >= 94) {
                        return 5;
                    }
                    return -5;

                case "PAIN_SCALE":
                    // 疼痛指数：0-3 正常 +5，4-6 中等，7-10 严重
                    int pain = Integer.parseInt(value.trim());
                    if (pain <= 3) {
                        return 5;
                    } else if (pain <= 6) {
                        return 0;
                    }
                    return -5;

                default:
                    return 0;
            }
        } catch (Exception e) {
            return 0;
        }
    }
}
