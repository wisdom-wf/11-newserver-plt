package com.elderlycare.service.device.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.device.DeviceBindDTO;
import com.elderlycare.dto.device.DevicePushDTO;
import com.elderlycare.dto.device.DeviceQueryDTO;
import com.elderlycare.entity.device.Device;
import com.elderlycare.entity.device.DeviceBinding;
import com.elderlycare.entity.device.DevicePushLog;
import com.elderlycare.entity.elder.HealthMeasurement;
import com.elderlycare.mapper.device.DeviceBindingMapper;
import com.elderlycare.mapper.device.DeviceMapper;
import com.elderlycare.mapper.device.DevicePushLogMapper;
import com.elderlycare.mapper.elder.HealthMeasurementMapper;
import com.elderlycare.service.device.DeviceService;
import com.elderlycare.vo.device.DeviceBindingVO;
import com.elderlycare.vo.device.DeviceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceMapper deviceMapper;
    private final DeviceBindingMapper bindingMapper;
    private final DevicePushLogMapper pushLogMapper;
    private final HealthMeasurementMapper measurementMapper;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void handlePushData(DevicePushDTO dto) {
        String deviceSn = dto.getDeviceSn();

        // 1. 查找设备
        Device device = deviceMapper.selectBySn(deviceSn);
        if (device == null) {
            savePushLog(null, null, null, dto, "设备未注册: " + deviceSn);
            throw BusinessException.notFound("设备未注册: " + deviceSn);
        }

        // 2. 更新设备状态
        device.setStatus("ACTIVE");
        device.setLastPushTime(LocalDateTime.now());
        deviceMapper.updateById(device);

        // 3. 查找活跃绑定
        DeviceBinding binding = bindingMapper.selectActiveByDeviceId(device.getDeviceId());
        if (binding == null) {
            savePushLog(device.getDeviceId(), null, null, dto, "设备未绑定到客户");
            return;
        }

        // 4. 解析数据，创建测量记录
        try {
            HealthMeasurement measurement = parseMeasurement(dto, binding);
            measurementMapper.insert(measurement);

            // 5. 记录成功日志
            DevicePushLog pushLog = new DevicePushLog();
            pushLog.setPushId(UUID.randomUUID().toString().replace("-", ""));
            pushLog.setDeviceId(device.getDeviceId());
            pushLog.setBindingId(binding.getBindingId());
            pushLog.setElderId(binding.getElderId());
            pushLog.setRawData(objectMapper.writeValueAsString(dto));
            pushLog.setMeasurementId(measurement.getMeasurementId());
            pushLog.setPushTime(LocalDateTime.now());
            pushLog.setProcessStatus("SUCCESS");
            pushLogMapper.insert(pushLog);

            log.info("设备数据处理成功: deviceSn={}, elderId={}, type={}", deviceSn, binding.getElderId(), binding.getMeasurementType());
        } catch (Exception e) {
            log.error("设备数据处理失败", e);
            savePushLog(device.getDeviceId(), binding.getBindingId(), binding.getElderId(), dto, e.getMessage());
        }
    }

    @Override
    @Transactional
    public DeviceBindingVO bindDevice(DeviceBindDTO dto) {
        // 查找设备
        Device device = deviceMapper.selectBySn(dto.getDeviceSn());
        if (device == null) {
            throw BusinessException.notFound("设备未注册: " + dto.getDeviceSn());
        }

        // 检查是否已绑定
        DeviceBinding existing = bindingMapper.selectActiveBinding(device.getDeviceId(), dto.getElderId(), dto.getMeasurementType());
        if (existing != null) {
            throw BusinessException.fail("该设备已绑定到此客户的" + getMeasurementTypeName(dto.getMeasurementType()));
        }

        // 创建绑定
        DeviceBinding binding = new DeviceBinding();
        binding.setBindingId(UUID.randomUUID().toString().replace("-", ""));
        binding.setDeviceId(device.getDeviceId());
        binding.setElderId(dto.getElderId());
        binding.setMeasurementType(dto.getMeasurementType());
        binding.setBindTime(LocalDateTime.now());
        binding.setStatus("ACTIVE");
        binding.setRemark(dto.getRemark());
        bindingMapper.insert(binding);

        // 更新设备状态
        device.setStatus("ACTIVE");
        deviceMapper.updateById(device);

        // 返回VO
        DeviceBindingVO vo = new DeviceBindingVO();
        BeanUtils.copyProperties(binding, vo);
        vo.setDeviceSn(device.getDeviceSn());
        vo.setDeviceType(device.getDeviceType());
        vo.setDeviceTypeName(getDeviceTypeName(device.getDeviceType()));
        vo.setDeviceName(device.getDeviceName());
        vo.setMeasurementTypeName(getMeasurementTypeName(dto.getMeasurementType()));
        return vo;
    }

    @Override
    @Transactional
    public void unbindDevice(String bindingId) {
        DeviceBinding binding = bindingMapper.selectById(bindingId);
        if (binding == null) {
            throw BusinessException.notFound("绑定记录不存在");
        }
        binding.setStatus("UNBOUND");
        binding.setUnbindTime(LocalDateTime.now());
        bindingMapper.updateById(binding);
    }

    @Override
    public PageResult<DeviceVO> getDeviceList(DeviceQueryDTO query) {
        QueryWrapper<Device> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (query.getDeviceType() != null && !query.getDeviceType().isEmpty()) {
            wrapper.eq("device_type", query.getDeviceType());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq("status", query.getStatus());
        }
        wrapper.orderByDesc("create_time");

        Page<Device> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<Device> result = deviceMapper.selectPage(page, wrapper);

        List<DeviceVO> voList = new ArrayList<>();
        for (Device d : result.getRecords()) {
            DeviceVO vo = new DeviceVO();
            BeanUtils.copyProperties(d, vo);
            vo.setStatusText(getStatusText(d.getStatus()));
            voList.add(vo);
        }
        return new PageResult<>(result.getTotal(), query.getPage(), query.getPageSize(), voList);
    }

    @Override
    public DeviceVO getDeviceBySn(String deviceSn) {
        Device device = deviceMapper.selectBySn(deviceSn);
        if (device == null) return null;
        DeviceVO vo = new DeviceVO();
        BeanUtils.copyProperties(device, vo);
        vo.setStatusText(getStatusText(device.getStatus()));
        return vo;
    }

    @Override
    public List<DeviceBindingVO> getElderDevices(String elderId) {
        List<DeviceBinding> bindings = bindingMapper.selectByElderId(elderId);
        List<DeviceBindingVO> voList = new ArrayList<>();
        for (DeviceBinding b : bindings) {
            DeviceBindingVO vo = new DeviceBindingVO();
            BeanUtils.copyProperties(b, vo);
            vo.setMeasurementTypeName(getMeasurementTypeName(b.getMeasurementType()));
            voList.add(vo);
        }
        return voList;
    }

    @Override
    public PageResult<DeviceBindingVO> getBindingList(DeviceQueryDTO query) {
        QueryWrapper<DeviceBinding> wrapper = new QueryWrapper<>();
        wrapper.eq("deleted", 0);
        if (query.getElderId() != null && !query.getElderId().isEmpty()) {
            wrapper.eq("elder_id", query.getElderId());
        }
        wrapper.orderByDesc("bind_time");

        Page<DeviceBinding> page = new Page<>(query.getPage(), query.getPageSize());
        IPage<DeviceBinding> result = bindingMapper.selectPage(page, wrapper);

        List<DeviceBindingVO> voList = new ArrayList<>();
        for (DeviceBinding b : result.getRecords()) {
            DeviceBindingVO vo = new DeviceBindingVO();
            BeanUtils.copyProperties(b, vo);
            vo.setMeasurementTypeName(getMeasurementTypeName(b.getMeasurementType()));
            voList.add(vo);
        }
        return new PageResult<>(result.getTotal(), query.getPage(), query.getPageSize(), voList);
    }

    // ========== 私有方法 ==========

    private HealthMeasurement parseMeasurement(DevicePushDTO dto, DeviceBinding binding) {
        HealthMeasurement m = new HealthMeasurement();
        m.setMeasurementId(UUID.randomUUID().toString().replace("-", ""));
        m.setElderId(binding.getElderId());
        m.setMeasurementType(binding.getMeasurementType());
        m.setMeasuredAt(LocalDateTime.now());
        m.setRemark("设备推送");

        Map<String, Object> data = dto.getData();
        if (data == null) throw new BusinessException("推送数据为空");

        switch (binding.getMeasurementType()) {
            case "BLOOD_PRESSURE":
                m.setMeasurementValue(data.get("sys") + "/" + data.get("dia"));
                m.setMeasurementUnit("mmHg");
                break;
            case "BLOOD_GLUCOSE":
                m.setMeasurementValue(String.valueOf(data.get("value")));
                m.setMeasurementUnit("mmol/L");
                break;
            case "WEIGHT":
                m.setMeasurementValue(String.valueOf(data.get("value")));
                m.setMeasurementUnit("kg");
                break;
            case "TEMPERATURE":
                m.setMeasurementValue(String.valueOf(data.get("value")));
                m.setMeasurementUnit("°C");
                break;
            case "PULSE":
                m.setMeasurementValue(String.valueOf(data.get("value")));
                m.setMeasurementUnit("bpm");
                break;
            case "SPO2":
                m.setMeasurementValue(String.valueOf(data.get("value")));
                m.setMeasurementUnit("%");
                break;
            default:
                throw new BusinessException("不支持的测量类型: " + binding.getMeasurementType());
        }
        return m;
    }

    private void savePushLog(String deviceId, String bindingId, String elderId, DevicePushDTO dto, String errorMsg) {
        try {
            DevicePushLog pushLog = new DevicePushLog();
            pushLog.setPushId(UUID.randomUUID().toString().replace("-", ""));
            pushLog.setDeviceId(deviceId);
            pushLog.setBindingId(bindingId);
            pushLog.setElderId(elderId);
            pushLog.setRawData(objectMapper.writeValueAsString(dto));
            pushLog.setPushTime(LocalDateTime.now());
            pushLog.setProcessStatus("FAILED");
            pushLog.setErrorMsg(errorMsg);
            pushLogMapper.insert(pushLog);
        } catch (Exception e) {
            log.error("保存推送日志失败", e);
        }
    }

    private String getDeviceTypeName(String type) {
        Map<String, String> map = new HashMap<>();
        map.put("BP", "血压计");
        map.put("BG", "血糖仪");
        map.put("WT", "体重秤");
        map.put("TP", "体温计");
        map.put("PL", "脉搏仪");
        map.put("SP", "血氧仪");
        return map.getOrDefault(type, type);
    }

    private String getMeasurementTypeName(String type) {
        Map<String, String> map = new HashMap<>();
        map.put("BLOOD_PRESSURE", "血压");
        map.put("BLOOD_GLUCOSE", "血糖");
        map.put("WEIGHT", "体重");
        map.put("TEMPERATURE", "体温");
        map.put("PULSE", "脉搏");
        map.put("SPO2", "血氧");
        return map.getOrDefault(type, type);
    }

    private String getStatusText(String status) {
        Map<String, String> map = new HashMap<>();
        map.put("INACTIVE", "未激活");
        map.put("ACTIVE", "在线");
        map.put("OFFLINE", "离线");
        map.put("FAULT", "故障");
        return map.getOrDefault(status, status);
    }
}
