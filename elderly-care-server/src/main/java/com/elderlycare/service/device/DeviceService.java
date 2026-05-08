package com.elderlycare.service.device;

import com.elderlycare.dto.device.DeviceBindDTO;
import com.elderlycare.dto.device.DevicePushDTO;
import com.elderlycare.dto.device.DeviceQueryDTO;
import com.elderlycare.common.PageResult;
import com.elderlycare.vo.device.DeviceBindingVO;
import com.elderlycare.vo.device.DeviceVO;

import java.util.List;

public interface DeviceService {

    /** 处理设备推送数据 */
    void handlePushData(DevicePushDTO dto);

    /** 绑定设备到客户 */
    DeviceBindingVO bindDevice(DeviceBindDTO dto);

    /** 解绑设备 */
    void unbindDevice(String bindingId);

    /** 获取设备列表 */
    PageResult<DeviceVO> getDeviceList(DeviceQueryDTO query);

    /** 根据序列号查询设备 */
    DeviceVO getDeviceBySn(String deviceSn);

    /** 获取客户的设备绑定列表 */
    List<DeviceBindingVO> getElderDevices(String elderId);

    /** 获取绑定列表 */
    PageResult<DeviceBindingVO> getBindingList(DeviceQueryDTO query);
}
