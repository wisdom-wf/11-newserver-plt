package com.elderlycare.controller.statistics;

import com.elderlycare.common.Result;
import com.elderlycare.common.UserContext;
import com.elderlycare.service.statistics.StatisticsService;
import com.elderlycare.vo.statistics.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * 数据统计Controller
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 仪表盘数据
     * 返回: 老人总数、服务商总数、服务人员总数、订单总数
     * 今日概况: 今日订单、已完成、待处理、已取消
     * 服务类型分布、趋势数据、TOP服务商
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboardData() {
        DashboardVO data = statisticsService.getDashboardData();
        return Result.success(data);
    }

    /**
     * 老人统计
     * 返回: 老人总数、新增老人、活跃老人
     * 按年龄段分布、按护理级别分布、按服务需求分布
     */
    @GetMapping("/elder")
    public Result<ElderStatisticsVO> getElderStatistics() {
        ElderStatisticsVO data = statisticsService.getElderStatistics();
        return Result.success(data);
    }

    /**
     * 服务商统计
     * 返回: 服务商总数、待审核、已审核
     * 按类型分布、TOP服务商排名
     */
    @GetMapping("/provider")
    public Result<ProviderStatisticsVO> getProviderStatistics() {
        ProviderStatisticsVO data = statisticsService.getProviderStatistics();
        return Result.success(data);
    }

    /**
     * 订单统计
     * 支持按日期范围、分组维度筛选
     * 返回: 订单总数、完成率、平均评分
     * 按服务类型分布、趋势数据
     *
     * @param startDate       开始日期
     * @param endDate         结束日期
     * @param groupBy         分组维度：day(按天), week(按周), month(按月)
     * @param serviceTypeCode 服务类型编码
     */
    @GetMapping("/order")
    public Result<OrderStatisticsVO> getOrderStatistics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false, defaultValue = "day") String groupBy,
            @RequestParam(required = false) String serviceTypeCode) {
        String start = startDate != null ? startDate.toString() : null;
        String end = endDate != null ? endDate.toString() : null;
        // 数据隔离：PROVIDER用户强制只看自己服务商
        String providerId = UserContext.getProviderId();
        OrderStatisticsVO data = statisticsService.getOrderStatistics(providerId, start, end, groupBy, serviceTypeCode);
        return Result.success(data);
    }

    /**
     * 财务统计
     * 返回: 总金额、政府补贴、自付金额、平台费
     * 月度趋势、按服务类型分布
     */
    @GetMapping("/financial")
    public Result<FinancialStatisticsVO> getFinancialStatistics() {
        FinancialStatisticsVO data = statisticsService.getFinancialStatistics();
        return Result.success(data);
    }

    /**
     * 质量统计
     * 返回: 平均评分、好评率、投诉率
     * 评分趋势、常见投诉类型
     */
    @GetMapping("/quality")
    public Result<QualityStatisticsVO> getQualityStatistics() {
        QualityStatisticsVO data = statisticsService.getQualityStatistics();
        return Result.success(data);
    }

    /**
     * 服务人员统计
     * 返回: 总数、在职、待上岗、已离职
     */
    @GetMapping("/staff")
    public Result<StaffStatisticsVO> getStaffStatistics() {
        StaffStatisticsVO data = statisticsService.getStaffStatistics();
        return Result.success(data);
    }
}
