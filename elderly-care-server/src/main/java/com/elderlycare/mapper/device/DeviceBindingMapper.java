package com.elderlycare.mapper.device;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.device.DeviceBinding;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DeviceBindingMapper extends BaseMapper<DeviceBinding> {
    DeviceBinding selectActiveByDeviceId(@Param("deviceId") String deviceId);
    List<DeviceBinding> selectByElderId(@Param("elderId") String elderId);
    DeviceBinding selectActiveBinding(@Param("deviceId") String deviceId, @Param("elderId") String elderId, @Param("measurementType") String measurementType);
}
