package com.elderlycare.service.appointment.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.appointment.AppointmentCreateDTO;
import com.elderlycare.dto.appointment.AppointmentQueryDTO;
import com.elderlycare.entity.appointment.Appointment;
import com.elderlycare.entity.elder.Elder;
import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.order.OrderStatus;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.mapper.appointment.AppointmentMapper;
import com.elderlycare.mapper.elder.ElderMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.provider.ProviderMapper;
import com.elderlycare.service.appointment.AppointmentService;
import com.elderlycare.vo.appointment.AppointmentStatisticsVO;
import com.elderlycare.vo.appointment.AppointmentVO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 预约服务实现
 */
@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {

    private final AppointmentMapper appointmentMapper;
    private final OrderMapper orderMapper;
    private final ElderMapper elderMapper;
    private final ProviderMapper providerMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createAppointment(AppointmentCreateDTO dto) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(IDGenerator.generateId());
        appointment.setAppointmentNo(generateAppointmentNo());
        appointment.setElderName(dto.getElderName());
        appointment.setElderIdCard(dto.getElderIdCard());
        appointment.setElderPhone(dto.getElderPhone());
        appointment.setElderAddress(dto.getElderAddress());
        appointment.setElderAreaId(dto.getElderAreaId());
        appointment.setElderAreaName(dto.getElderAreaName());
        appointment.setServiceType(dto.getServiceType());
        appointment.setServiceTypeCode(dto.getServiceTypeCode());
        appointment.setServiceContent(dto.getServiceContent());
        appointment.setAppointmentTime(dto.getAppointmentTime());
        appointment.setServiceDuration(dto.getServiceDuration());
        appointment.setProviderId(dto.getProviderId());
        appointment.setProviderName(dto.getProviderName());
        appointment.setVisitorCount(dto.getVisitorCount());
        appointment.setRemark(dto.getRemark());
        appointment.setAssessmentType(dto.getAssessmentType());
        appointment.setStatus("PENDING");
        appointment.setValidity("VALID");
        appointment.setCreateTime(LocalDateTime.now());

        appointmentMapper.insert(appointment);
        return appointment.getAppointmentId();
    }

    @Override
    public PageResult<AppointmentVO> getAppointmentList(AppointmentQueryDTO query) {
        Page<Appointment> page = new Page<>(query.getCurrent(), query.getPageSize());
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();

        if (query.getAppointmentNo() != null && !query.getAppointmentNo().isEmpty()) {
            wrapper.eq(Appointment::getAppointmentNo, query.getAppointmentNo());
        }
        if (query.getElderName() != null && !query.getElderName().isEmpty()) {
            wrapper.like(Appointment::getElderName, query.getElderName());
        }
        if (query.getElderPhone() != null && !query.getElderPhone().isEmpty()) {
            wrapper.eq(Appointment::getElderPhone, query.getElderPhone());
        }
        if (query.getServiceTypeCode() != null && !query.getServiceTypeCode().isEmpty()) {
            wrapper.eq(Appointment::getServiceTypeCode, query.getServiceTypeCode());
        }
        if (query.getStatus() != null && !query.getStatus().isEmpty()) {
            wrapper.eq(Appointment::getStatus, query.getStatus());
        }
        if (query.getProviderId() != null && !query.getProviderId().isEmpty()) {
            wrapper.eq(Appointment::getProviderId, query.getProviderId());
        }

        wrapper.orderByDesc(Appointment::getCreateTime);

        IPage<Appointment> result = appointmentMapper.selectPage(page, wrapper);

        List<AppointmentVO> voList = result.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), (int) result.getCurrent(), (int) result.getSize(), voList);
    }

    @Override
    public AppointmentVO getAppointment(String id) {
        Appointment appointment = appointmentMapper.selectById(id);
        return convertToVO(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmAppointment(String id, String providerId, String appointmentTime) {
        // 1. 先查询预约详情
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 状态校验：只有PENDING状态可以确认
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new RuntimeException("只有待确认状态的预约可以进行确认操作");
        }

        // 3. 参数校验
        if (providerId == null || providerId.isEmpty()) {
            throw new RuntimeException("请选择服务商");
        }
        if (appointmentTime == null || appointmentTime.isEmpty()) {
            throw new RuntimeException("请填写预约时间");
        }

        // 4. 查询服务商信息并校验状态
        Provider provider = providerMapper.selectById(providerId);
        if (provider == null) {
            throw new RuntimeException("所选服务商不存在");
        }
        if (!"ENABLED".equals(provider.getStatus())) {
            throw new RuntimeException("所选服务商已被禁用，无法确认预约");
        }
        appointment.setProviderId(providerId);
        appointment.setProviderName(provider.getProviderName());
        appointment.setProviderAddress(provider.getAddress());
        appointment.setAppointmentTime(appointmentTime);
        appointment.setStatus("CONFIRMED");
        appointment.setConfirmTime(LocalDateTime.now());
        appointmentMapper.updateById(appointment);

        // 5. 自动创建老人档案（如果不存在）
        Elder elder = createOrUpdateElderFromAppointment(appointment, providerId);

        // 6. 自动生成订单
        createOrderFromAppointment(appointment, elder);
    }

    /**
     * 根据预约创建或更新老人档案
     */
    private Elder createOrUpdateElderFromAppointment(Appointment appointment, String providerId) {
        // 根据身份证号查询是否已存在老人档案
        Elder elder = null;
        if (appointment.getElderIdCard() != null && !appointment.getElderIdCard().isEmpty()) {
            elder = elderMapper.selectByIdCard(appointment.getElderIdCard());
        }

        if (elder == null && appointment.getElderPhone() != null && !appointment.getElderPhone().isEmpty()) {
            // 根据手机号查询
            elder = elderMapper.selectByPhone(appointment.getElderPhone());
        }

        if (elder == null) {
            // 创建新档案
            elder = new Elder();
            elder.setElderId(IDGenerator.generateId());
            elder.setName(appointment.getElderName());
            elder.setIdCard(appointment.getElderIdCard());
            elder.setPhone(appointment.getElderPhone());
            elder.setAddress(appointment.getElderAddress());
            elder.setStatus("ACTIVE");
            elder.setRegisterDate(LocalDate.now());
            elder.setProviderId(providerId); // 关联服务商
            elder.setCreateTime(LocalDateTime.now());

            // 根据身份证号自动计算性别、年龄和出生日期
            if (appointment.getElderIdCard() != null && appointment.getElderIdCard().length() == 18) {
                String idCard = appointment.getElderIdCard();
                try {
                    // 计算性别：第17位奇数为男，偶数为女
                    int genderCode = Character.getNumericValue(idCard.charAt(16));
                    elder.setGender(genderCode % 2 == 1 ? "MALE" : "FEMALE");

                    // 计算出生日期和年龄
                    String birthYearStr = idCard.substring(6, 10);
                    String birthMonthStr = idCard.substring(10, 12);
                    String birthDayStr = idCard.substring(12, 14);
                    int birthYear = Integer.parseInt(birthYearStr);
                    int currentYear = LocalDate.now().getYear();
                    elder.setAge(currentYear - birthYear);
                    elder.setBirthDate(LocalDate.of(birthYear, Integer.parseInt(birthMonthStr), Integer.parseInt(birthDayStr)));
                } catch (Exception e) {
                    // 身份证号解析失败，不设置这些字段
                }
            }

            elderMapper.insert(elder);
        } else {
            // 更新现有档案，关联服务商
            elder.setProviderId(providerId);
            elder.setUpdateTime(LocalDateTime.now());
            elderMapper.updateById(elder);
        }

        return elder;
    }

    /**
     * 根据预约创建订单
     */
    private void createOrderFromAppointment(Appointment appointment, Elder elder) {
        // 生成订单编号
        String orderNo = generateOrderNo();

        // 解析预约时间为日期和时间
        String appointmentTime = appointment.getAppointmentTime();
        LocalDate serviceDate = LocalDate.now(); // 默认今天
        String serviceTime = "09:00:00"; // 默认时间

        if (appointmentTime != null && !appointmentTime.isEmpty()) {
            try {
                // 尝试解析 "2024-04-15 10:00:00" 格式
                if (appointmentTime.contains(" ")) {
                    String[] parts = appointmentTime.split(" ");
                    if (parts.length >= 1) {
                        serviceDate = LocalDate.parse(parts[0]);
                    }
                    if (parts.length >= 2) {
                        serviceTime = parts[1];
                    }
                } else {
                    serviceDate = LocalDate.parse(appointmentTime);
                }
            } catch (Exception e) {
                // 解析失败，使用默认值
            }
        }

        // 创建订单
        Order order = new Order();
        order.setOrderId(IDGenerator.generateId());
        order.setOrderNo(orderNo);
        order.setElderId(elder.getElderId()); // 关联老人档案ID
        order.setElderName(appointment.getElderName());
        order.setElderPhone(appointment.getElderPhone());
        order.setServiceTypeCode(appointment.getServiceTypeCode());
        order.setServiceTypeName(appointment.getServiceType());
        order.setServiceDate(serviceDate);
        order.setServiceTime(serviceTime);
        order.setServiceDuration(appointment.getServiceDuration());
        order.setServiceAddress(appointment.getElderAddress());
        order.setOrderType("NORMAL");
        order.setOrderSource("APPOINTMENT"); // 标记来源为预约
        order.setSubsidyType("SELF_PAY"); // 默认自费
        order.setStatus(OrderStatus.CREATED.getCode());
        order.setProviderId(appointment.getProviderId());
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);

        // 回写订单ID到预约记录
        appointment.setOrderId(order.getOrderId());
        appointment.setOrderNo(order.getOrderNo());
        appointmentMapper.updateById(appointment);
    }

    /**
     * 生成订单编号
     */
    private String generateOrderNo() {
        String prefix = "ORD";
        String date = new java.text.SimpleDateFormat("yyyyMMdd").format(new java.util.Date());
        String random = String.format("%04d", (int) (Math.random() * 10000));
        return prefix + date + random;
    }

    @Override
    public void assignAppointment(String id, String providerId) {
        // 1. 查询预约详情
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 状态校验：只有CONFIRMED状态可以分配
        if (!"CONFIRMED".equals(appointment.getStatus())) {
            throw new RuntimeException("只有已确认状态的预约可以进行分配");
        }

        // 3. 参数校验
        if (providerId == null || providerId.isEmpty()) {
            throw new RuntimeException("请选择服务商");
        }

        // 4. 校验服务商状态
        Provider provider = providerMapper.selectById(providerId);
        if (provider == null) {
            throw new RuntimeException("所选服务商不存在");
        }
        if (!"ENABLED".equals(provider.getStatus())) {
            throw new RuntimeException("所选服务商已被禁用，无法分配");
        }

        // 5. 执行分配
        appointment.setProviderId(providerId);
        appointment.setStatus("ASSIGNED");
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void cancelAppointment(String id, String reason) {
        // 1. 查询预约详情
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 状态校验：只有PENDING状态可以取消
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new RuntimeException("只有待确认状态的预约可以取消");
        }

        // 3. 执行取消
        appointment.setCancelReason(reason);
        appointment.setStatus("CANCELLED");
        appointmentMapper.updateById(appointment);
    }

    @Override
    public void invalidateAppointment(String id, String reason) {
        // 1. 查询预约详情
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        // 2. 状态校验：只有PENDING状态可以作废
        if (!"PENDING".equals(appointment.getStatus())) {
            throw new RuntimeException("只有待确认状态的预约可以作废");
        }

        // 3. 执行作废
        appointment.setCancelReason(reason);
        appointment.setStatus("INVALID");
        appointment.setValidity("INVALID");
        appointmentMapper.updateById(appointment);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> importAppointment(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 跳过表头，从第二行开始
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Appointment appointment = parseAppointmentRow(row);
                    appointmentMapper.insert(appointment);
                    successCount++;
                } catch (Exception e) {
                    failCount++;
                    errors.add("第" + (i + 1) + "行解析失败: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Excel文件读取失败", e);
        }

        result.put("successCount", successCount);
        result.put("failCount", failCount);
        result.put("errors", errors);
        return result;
    }

    private Appointment parseAppointmentRow(Row row) {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(IDGenerator.generateId());
        appointment.setAppointmentNo(generateAppointmentNo());
        appointment.setStatus("PENDING");
        appointment.setValidity("VALID");

        // 姓名
        appointment.setElderName(getCellStringValue(row.getCell(0)));
        // 身份证号
        appointment.setElderIdCard(getCellStringValue(row.getCell(1)));
        // 手机号
        String phone = getCellStringValue(row.getCell(2));
        if (phone != null && phone.length() == 11) {
            appointment.setElderPhone(phone);
        }
        // 预约服务类型
        String serviceType = getCellStringValue(row.getCell(3));
        appointment.setServiceType(serviceType);
        appointment.setServiceTypeCode(getServiceTypeCode(serviceType));
        // 服务内容类型
        appointment.setServiceContent(getCellStringValue(row.getCell(4)));
        // 预约时间
        appointment.setAppointmentTime(getCellStringValue(row.getCell(5)));
        // 服务地址
        String address = getCellStringValue(row.getCell(6));
        appointment.setElderAddress(address);
        // 解析地址获取区域
        parseAddressArea(address, appointment);
        // 创建时间
        Cell createTimeCell = row.getCell(7);
        if (createTimeCell != null) {
            LocalDateTime createTime = getCellDateValue(createTimeCell);
            if (createTime != null) {
                appointment.setCreateTime(createTime);
            }
        }
        if (appointment.getCreateTime() == null) {
            appointment.setCreateTime(LocalDateTime.now());
        }

        return appointment;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().toString();
                }
                double numVal = cell.getNumericCellValue();
                if (numVal == Math.floor(numVal)) {
                    return String.valueOf((long) numVal);
                }
                return String.valueOf(numVal);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    private LocalDateTime getCellDateValue(Cell cell) {
        if (cell == null) return null;
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private String generateAppointmentNo() {
        return "APT" + System.currentTimeMillis();
    }

    private String getServiceTypeCode(String serviceType) {
        if (serviceType == null) return null;
        switch (serviceType) {
            case "上门服务": return "DOOR_TO_DOOR";
            case "日间照料": return "DAY_CARE";
            case "助餐服务": return "MEAL";
            case "助洁服务": return "CLEANING";
            case "助浴服务": return "BATHING";
            case "健康监测": return "HEALTH";
            case "康复护理": return "REHAB";
            case "精神慰藉": return "COMFORT";
            case "信息咨询": return "INFO";
            case "紧急救援": return "EMERGENCY";
            default: return "OTHER";
        }
    }

    private void parseAddressArea(String address, Appointment appointment) {
        if (address == null) return;
        // 地址格式: 陕西省延安市宝塔区xxx
        if (address.contains("宝塔区")) {
            appointment.setElderAreaName("宝塔区");
            appointment.setElderAreaId("610602");
        } else if (address.contains("安塞区")) {
            appointment.setElderAreaName("安塞区");
            appointment.setElderAreaId("610603");
        } else if (address.contains("子长市")) {
            appointment.setElderAreaName("子长市");
            appointment.setElderAreaId("610681");
        } else if (address.contains("延长县")) {
            appointment.setElderAreaName("延长县");
            appointment.setElderAreaId("610621");
        } else if (address.contains("延川县")) {
            appointment.setElderAreaName("延川县");
            appointment.setElderAreaId("610622");
        } else if (address.contains("志丹县")) {
            appointment.setElderAreaName("志丹县");
            appointment.setElderAreaId("610625");
        } else if (address.contains("吴起县")) {
            appointment.setElderAreaName("吴起县");
            appointment.setElderAreaId("610626");
        } else if (address.contains("甘泉县")) {
            appointment.setElderAreaName("甘泉县");
            appointment.setElderAreaId("610627");
        } else if (address.contains("富县")) {
            appointment.setElderAreaName("富县");
            appointment.setElderAreaId("610628");
        } else if (address.contains("洛川县")) {
            appointment.setElderAreaName("洛川县");
            appointment.setElderAreaId("610629");
        } else if (address.contains("黄陵县")) {
            appointment.setElderAreaName("黄陵县");
            appointment.setElderAreaId("610630");
        } else if (address.contains("黄龙县")) {
            appointment.setElderAreaName("黄龙县");
            appointment.setElderAreaId("610631");
        } else {
            appointment.setElderAreaName("延安市");
            appointment.setElderAreaId("610600");
        }
    }

    @Override
    public byte[] generateTemplate() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("预约导入模板");
            // Header row
            String[] headers = {"老人姓名", "身份证号", "联系电话", "服务类型", "预约日期", "预约时段", "服务地址", "备注"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                sheet.setColumnWidth(i, 20 * 256);
            }
            // Example row
            Row exampleRow = sheet.createRow(1);
            exampleRow.createCell(0).setCellValue("张三");
            exampleRow.createCell(1).setCellValue("612601195001010011");
            exampleRow.createCell(2).setCellValue("13800138000");
            exampleRow.createCell(3).setCellValue("上门服务");
            exampleRow.createCell(4).setCellValue("2026-04-20");
            exampleRow.createCell(5).setCellValue("上午");
            exampleRow.createCell(6).setCellValue("陕西省延安市宝塔区XX街道XX社区");
            exampleRow.createCell(7).setCellValue("");
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("生成模板失败", e);
        }
    }

    @Override
    public AppointmentStatisticsVO getStatistics(String providerId, String areaId, String startDate, String endDate) {
        AppointmentStatisticsVO stats = new AppointmentStatisticsVO();

        // 构建基础过滤条件
        LambdaQueryWrapper<Appointment> baseWrapper = new LambdaQueryWrapper<>();
        if (providerId != null) {
            baseWrapper.eq(Appointment::getProviderId, providerId);
        }
        if (areaId != null && !areaId.isEmpty()) {
            baseWrapper.eq(Appointment::getElderAreaId, areaId);
        }
        if (startDate != null && !startDate.isEmpty()) {
            baseWrapper.ge(Appointment::getCreateTime, LocalDateTime.parse(startDate + "T00:00:00"));
        }
        if (endDate != null && !endDate.isEmpty()) {
            baseWrapper.le(Appointment::getCreateTime, LocalDateTime.parse(endDate + "T23:59:59"));
        }

        // 总数
        stats.setTotal(appointmentMapper.selectCount(baseWrapper.clone()).intValue());

        // 待处理
        LambdaQueryWrapper<Appointment> w1 = baseWrapper.clone();
        w1.eq(Appointment::getStatus, "PENDING");
        stats.setPending(appointmentMapper.selectCount(w1).intValue());

        // 已确认
        LambdaQueryWrapper<Appointment> w2 = baseWrapper.clone();
        w2.eq(Appointment::getStatus, "CONFIRMED");
        stats.setConfirmed(appointmentMapper.selectCount(w2).intValue());

        // 已分配
        LambdaQueryWrapper<Appointment> w3 = baseWrapper.clone();
        w3.eq(Appointment::getStatus, "ASSIGNED");
        stats.setAssigned(appointmentMapper.selectCount(w3).intValue());

        // 已完成
        LambdaQueryWrapper<Appointment> w4 = baseWrapper.clone();
        w4.eq(Appointment::getStatus, "COMPLETED");
        stats.setCompleted(appointmentMapper.selectCount(w4).intValue());

        // 已取消
        LambdaQueryWrapper<Appointment> w5 = baseWrapper.clone();
        w5.eq(Appointment::getStatus, "CANCELLED");
        stats.setCancelled(appointmentMapper.selectCount(w5).intValue());

        // 已作废
        LambdaQueryWrapper<Appointment> w6 = baseWrapper.clone();
        w6.eq(Appointment::getStatus, "INVALID");
        stats.setInvalid(appointmentMapper.selectCount(w6).intValue());

        return stats;
    }

    @Override
    public List<AppointmentVO> getAppointmentsByPhone(String phone) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getElderPhone, phone);
        wrapper.orderByDesc(Appointment::getCreateTime);

        List<Appointment> list = appointmentMapper.selectList(wrapper);
        return list.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    private AppointmentVO convertToVO(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        AppointmentVO vo = new AppointmentVO();
        vo.setAppointmentId(appointment.getAppointmentId());
        vo.setId(appointment.getAppointmentId());
        vo.setAppointmentNo(appointment.getAppointmentNo());
        vo.setElderName(appointment.getElderName());
        vo.setElderIdCard(appointment.getElderIdCard());
        vo.setElderPhone(appointment.getElderPhone());
        vo.setElderAddress(appointment.getElderAddress());
        vo.setElderAreaId(appointment.getElderAreaId());
        vo.setElderAreaName(appointment.getElderAreaName());
        vo.setServiceType(appointment.getServiceType());
        vo.setServiceTypeCode(appointment.getServiceTypeCode());
        vo.setServiceContent(appointment.getServiceContent());
        vo.setAppointmentTime(appointment.getAppointmentTime());
        vo.setServiceDuration(appointment.getServiceDuration());
        vo.setProviderId(appointment.getProviderId());
        vo.setProviderName(appointment.getProviderName());
        vo.setProviderAddress(appointment.getProviderAddress());
        vo.setVisitorCount(appointment.getVisitorCount());
        vo.setRemark(appointment.getRemark());
        vo.setStatus(appointment.getStatus());
        vo.setValidity(appointment.getValidity());
        vo.setCancelReason(appointment.getCancelReason());
        vo.setReplyInfo(appointment.getReplyInfo());
        vo.setAssessmentType(appointment.getAssessmentType());
        vo.setCreateTime(appointment.getCreateTime() != null ? appointment.getCreateTime().toString() : null);
        vo.setConfirmTime(appointment.getConfirmTime() != null ? appointment.getConfirmTime().toString() : null);
        vo.setUpdateTime(appointment.getUpdateTime() != null ? appointment.getUpdateTime().toString() : null);
        return vo;
    }

    @Override
    public Object getAppointmentTimeline(String id) {
        Appointment appointment = appointmentMapper.selectById(id);
        if (appointment == null) {
            throw new RuntimeException("预约不存在");
        }

        Map<String, Object> timeline = new HashMap<>();
        timeline.put("appointmentId", appointment.getAppointmentId());
        timeline.put("appointmentNo", appointment.getAppointmentNo());
        timeline.put("currentStatus", appointment.getStatus());
        timeline.put("currentStatusName", getStatusName(appointment.getStatus()));

        // 填充订单关联信息
        if (appointment.getOrderId() != null && !appointment.getOrderId().isEmpty()) {
            Order order = orderMapper.selectById(appointment.getOrderId());
            if (order != null) {
                timeline.put("orderId", order.getOrderId());
                timeline.put("orderNo", order.getOrderNo());
                timeline.put("orderStatus", order.getStatus());
                timeline.put("orderStatusName", getOrderStatusName(order.getStatus()));
            }
        }

        // 构建预约时间轴节点
        List<Map<String, Object>> nodes = buildAppointmentTimelineNodes(appointment);
        timeline.put("nodes", nodes);

        // 追加订单时间轴节点
        if (appointment.getOrderId() != null && !appointment.getOrderId().isEmpty()) {
            Order order = orderMapper.selectById(appointment.getOrderId());
            if (order != null) {
                List<Map<String, Object>> orderNodes = buildOrderTimelineNodes(order);
                timeline.put("orderNodes", orderNodes);
            }
        }

        return timeline;
    }

    private String getOrderStatusName(String status) {
        if (status == null) return "";
        OrderStatus orderStatus = OrderStatus.fromCode(status);
        return orderStatus != null ? orderStatus.getDescription() : status;
    }

    private List<Map<String, Object>> buildOrderTimelineNodes(Order order) {
        List<Map<String, Object>> nodes = new java.util.ArrayList<>();
        String currentStatus = order.getStatus();

        // 创建（始终存在）
        Map<String, Object> created = new HashMap<>();
        created.put("status", "CREATED");
        created.put("statusName", "待派单");
        created.put("title", "订单已创建");
        created.put("time", order.getCreateTime() != null ? order.getCreateTime().toString() : null);
        created.put("completed", true);
        created.put("active", "CREATED".equals(currentStatus));
        List<Map<String, String>> createdDetails = new java.util.ArrayList<>();
        addDetail(createdDetails, "订单号", order.getOrderNo());
        addDetail(createdDetails, "服务类型", order.getServiceTypeName());
        addDetail(createdDetails, "服务日期", order.getServiceDate() != null ? order.getServiceDate().toString() : null);
        addDetail(createdDetails, "操作人", "系统");
        created.put("details", createdDetails);
        nodes.add(created);

        // 派单
        if (order.getDispatchTime() != null) {
            Map<String, Object> node = new HashMap<>();
            node.put("status", "DISPATCHED");
            node.put("statusName", "已派单");
            node.put("title", "订单已派单");
            node.put("time", order.getDispatchTime().toString());
            node.put("completed", true);
            node.put("active", "DISPATCHED".equals(currentStatus));
            List<Map<String, String>> details = new java.util.ArrayList<>();
            addDetail(details, "订单号", order.getOrderNo());
            addDetail(details, "服务商", order.getProviderName());
            addDetail(details, "服务人员", order.getStaffName());
            addDetail(details, "操作人", "管理员");
            node.put("details", details);
            nodes.add(node);
        }

        // 接单
        if (order.getReceiveTime() != null) {
            Map<String, Object> node = new HashMap<>();
            node.put("status", "RECEIVED");
            node.put("statusName", "已接单");
            node.put("title", "服务人员已接单");
            node.put("time", order.getReceiveTime().toString());
            node.put("completed", true);
            node.put("active", "RECEIVED".equals(currentStatus));
            List<Map<String, String>> details = new java.util.ArrayList<>();
            addDetail(details, "订单号", order.getOrderNo());
            addDetail(details, "服务人员", order.getStaffName());
            addDetail(details, "操作人", "服务人员");
            node.put("details", details);
            nodes.add(node);
        }

        // 服务开始
        if (order.getStartTime() != null) {
            Map<String, Object> node = new HashMap<>();
            node.put("status", "SERVICE_STARTED");
            node.put("statusName", "服务中");
            node.put("title", "服务已开始");
            node.put("time", order.getStartTime().toString());
            node.put("completed", true);
            node.put("active", "SERVICE_STARTED".equals(currentStatus));
            List<Map<String, String>> details = new java.util.ArrayList<>();
            addDetail(details, "订单号", order.getOrderNo());
            addDetail(details, "操作人", "服务人员");
            node.put("details", details);
            nodes.add(node);
        }

        // 服务完成
        if (order.getCompleteTime() != null) {
            Map<String, Object> node = new HashMap<>();
            node.put("status", "SERVICE_COMPLETED");
            node.put("statusName", "已完成");
            node.put("title", "服务已完成");
            node.put("time", order.getCompleteTime().toString());
            node.put("completed", true);
            node.put("active", "SERVICE_COMPLETED".equals(currentStatus));
            List<Map<String, String>> details = new java.util.ArrayList<>();
            addDetail(details, "订单号", order.getOrderNo());
            addDetail(details, "操作人", "服务人员");
            node.put("details", details);
            nodes.add(node);
        }

        // 取消
        if ("CANCELLED".equals(currentStatus)) {
            Map<String, Object> node = new HashMap<>();
            node.put("status", "CANCELLED");
            node.put("statusName", "已取消");
            node.put("title", "订单已取消");
            node.put("time", order.getUpdateTime() != null ? order.getUpdateTime().toString() : null);
            node.put("completed", true);
            node.put("active", true);
            List<Map<String, String>> details = new java.util.ArrayList<>();
            addDetail(details, "订单号", order.getOrderNo());
            addDetail(details, "取消原因", order.getCancelReason());
            addDetail(details, "操作人", "用户/管理员");
            node.put("details", details);
            nodes.add(node);
        }

        // 拒单
        if ("REJECTED".equals(currentStatus)) {
            Map<String, Object> node = new HashMap<>();
            node.put("status", "REJECTED");
            node.put("statusName", "已拒单");
            node.put("title", "订单已被拒单");
            node.put("time", order.getUpdateTime() != null ? order.getUpdateTime().toString() : null);
            node.put("completed", true);
            node.put("active", true);
            List<Map<String, String>> details = new java.util.ArrayList<>();
            addDetail(details, "订单号", order.getOrderNo());
            addDetail(details, "操作人", "服务人员");
            node.put("details", details);
            nodes.add(node);
        }

        return nodes;
    }

    private List<Map<String, Object>> buildAppointmentTimelineNodes(Appointment appointment) {
        List<Map<String, Object>> nodes = new java.util.ArrayList<>();

        // 创建
        Map<String, Object> createdNode = new HashMap<>();
        createdNode.put("status", "CREATED");
        createdNode.put("statusName", "已创建");
        createdNode.put("title", "预约已创建");
        createdNode.put("time", appointment.getCreateTime() != null ? appointment.getCreateTime().toString() : null);
        createdNode.put("completed", true);
        createdNode.put("active", "CREATED".equals(appointment.getStatus()));
        List<Map<String, String>> createdDetails = new java.util.ArrayList<>();
        addDetail(createdDetails, "预约单号", appointment.getAppointmentNo());
        addDetail(createdDetails, "老人姓名", appointment.getElderName());
        addDetail(createdDetails, "服务类型", appointment.getServiceType());
        addDetail(createdDetails, "预约时间", appointment.getAppointmentTime());
        createdNode.put("details", createdDetails);
        nodes.add(createdNode);

        // 确认
        if (appointment.getConfirmTime() != null || "CONFIRMED".equals(appointment.getStatus())) {
            Map<String, Object> confirmedNode = new HashMap<>();
            confirmedNode.put("status", "CONFIRMED");
            confirmedNode.put("statusName", "已确认");
            confirmedNode.put("title", "预约已确认");
            confirmedNode.put("time", appointment.getConfirmTime() != null ? appointment.getConfirmTime().toString() : null);
            confirmedNode.put("completed", true);
            confirmedNode.put("active", "CONFIRMED".equals(appointment.getStatus()));
            List<Map<String, String>> confirmedDetails = new java.util.ArrayList<>();
            addDetail(confirmedDetails, "服务机构", appointment.getProviderName());
            addDetail(confirmedDetails, "预约时间", appointment.getAppointmentTime());
            confirmedNode.put("details", confirmedDetails);
            nodes.add(confirmedNode);
        }

        // 完成
        if ("COMPLETED".equals(appointment.getStatus())) {
            Map<String, Object> completedNode = new HashMap<>();
            completedNode.put("status", "COMPLETED");
            completedNode.put("statusName", "已完成");
            completedNode.put("title", "服务已完成");
            completedNode.put("time", appointment.getUpdateTime() != null ? appointment.getUpdateTime().toString() : null);
            completedNode.put("completed", true);
            completedNode.put("active", false);
            completedNode.put("details", new java.util.ArrayList<>());
            nodes.add(completedNode);
        }

        // 取消/作废
        if ("CANCELLED".equals(appointment.getStatus()) || "INVALID".equals(appointment.getStatus())) {
            Map<String, Object> cancelledNode = new HashMap<>();
            cancelledNode.put("status", appointment.getStatus());
            cancelledNode.put("statusName", "CANCELLED".equals(appointment.getStatus()) ? "已取消" : "已作废");
            cancelledNode.put("title", "CANCELLED".equals(appointment.getStatus()) ? "预约已取消" : "预约已作废");
            cancelledNode.put("time", appointment.getUpdateTime() != null ? appointment.getUpdateTime().toString() : null);
            cancelledNode.put("completed", true);
            cancelledNode.put("active", true);
            List<Map<String, String>> cancelledDetails = new java.util.ArrayList<>();
            addDetail(cancelledDetails, "原因", appointment.getCancelReason());
            cancelledNode.put("details", cancelledDetails);
            nodes.add(cancelledNode);
        }

        return nodes;
    }

    private void addDetail(List<Map<String, String>> details, String label, String value) {
        if (value != null && !value.isEmpty()) {
            Map<String, String> detail = new HashMap<>();
            detail.put("label", label);
            detail.put("value", value);
            details.add(detail);
        }
    }

    private String getStatusName(String status) {
        if (status == null) return "";
        switch (status) {
            case "PENDING": return "待确认";
            case "CONFIRMED": return "已确认";
            case "ASSIGNED": return "已分配";
            case "COMPLETED": return "已完成";
            case "CANCELLED": return "已取消";
            case "INVALID": return "已作废";
            default: return status;
        }
    }
}
