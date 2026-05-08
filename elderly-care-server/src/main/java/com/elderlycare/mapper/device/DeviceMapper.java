package com.elderlycare.mapper.device;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.device.Device;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DeviceMapper extends BaseMapper<Device> {
    Device selectBySn(@Param("deviceSn") String deviceSn);
}
