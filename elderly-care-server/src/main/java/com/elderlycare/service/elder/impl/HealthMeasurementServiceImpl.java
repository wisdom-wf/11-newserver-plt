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
}
