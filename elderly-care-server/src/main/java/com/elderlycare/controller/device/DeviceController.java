package com.elderlycare.controller.device;

import com.elderlycare.common.PageResult;
import com.elderlycare.common.Result;
import com.elderlycare.dto.device.DeviceBindDTO;
import com.elderlycare.dto.device.DevicePushDTO;
import com.elderlycare.dto.device.DeviceQueryDTO;
import com.elderlycare.service.device.DeviceService;
import com.elderlycare.vo.device.DeviceBindingVO;
import com.elderlycare.vo.device.DeviceVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    /** 设备数据推送（无需认证，设备直接调用） */
    @PostMapping("/push")
    public Result<Void> handlePush(
            @RequestHeader("X-Device-SN") String deviceSn,
            @RequestBody DevicePushDTO dto
    ) {
        if (deviceSn == null || deviceSn.isBlank()) {
            log.warn("设备推送请求缺少X-Device-SN头");
            return Result.error("缺少设备序列号");
        }
        log.info("收到设备推送: deviceSn={}", deviceSn);
        dto.setDeviceSn(deviceSn);
        deviceService.handlePushData(dto);
        return Result.success();
    }

    /** 设备列表 */
    @GetMapping
    public Result<PageResult<DeviceVO>> getDeviceList(DeviceQueryDTO query) {
        return Result.success(deviceService.getDeviceList(query));
    }

    /** 根据序列号查询设备 */
    @GetMapping("/by-sn/{sn}")
    public Result<DeviceVO> getDeviceBySn(@PathVariable String sn) {
        return Result.success(deviceService.getDeviceBySn(sn));
    }

    /** 绑定设备到客户 */
    @PostMapping("/bind")
    public Result<DeviceBindingVO> bindDevice(@RequestBody DeviceBindDTO dto) {
        return Result.success(deviceService.bindDevice(dto));
    }

    /** 解绑设备 */
    @DeleteMapping("/bindings/{bindingId}")
    public Result<Void> unbindDevice(@PathVariable String bindingId) {
        deviceService.unbindDevice(bindingId);
        return Result.success();
    }

    /** 获取客户的设备绑定列表 */
    @GetMapping("/elder/{elderId}")
    public Result<List<DeviceBindingVO>> getElderDevices(@PathVariable String elderId) {
        return Result.success(deviceService.getElderDevices(elderId));
    }

    /** 绑定列表 */
    @GetMapping("/bindings")
    public Result<PageResult<DeviceBindingVO>> getBindingList(DeviceQueryDTO query) {
        return Result.success(deviceService.getBindingList(query));
    }
}
