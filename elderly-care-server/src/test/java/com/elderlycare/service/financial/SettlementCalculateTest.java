package com.elderlycare.service.financial;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.dto.financial.SettlementCalculateDTO;
import com.elderlycare.entity.financial.Settlement;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.financial.SettlementMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.financial.impl.SettlementServiceImpl;
import com.elderlycare.vo.financial.SettlementCalculateVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("财务结算计算测试")
class SettlementCalculateTest {

    @InjectMocks
    private SettlementServiceImpl settlementService;

    @Mock private SettlementMapper settlementMapper;
    @Mock private OrderMapper orderMapper;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(settlementService, "baseMapper", settlementMapper);
    }

    @Test
    @DisplayName("计算结算金额 - 正常场景")
    void shouldCalculateSettlementCorrectly() {
        SettlementCalculateDTO dto = new SettlementCalculateDTO();
        dto.setProviderId("P001");

        when(settlementMapper.selectList(any())).thenReturn(Collections.emptyList());

        Order order1 = createOrder("ORD001", new BigDecimal("100.00"),
            new BigDecimal("60.00"), new BigDecimal("40.00"));
        Order order2 = createOrder("ORD002", new BigDecimal("200.00"),
            new BigDecimal("120.00"), new BigDecimal("80.00"));

        when(orderMapper.selectList(any())).thenReturn(Arrays.asList(order1, order2));

        SettlementCalculateVO result = settlementService.calculateSettlement(dto);

        assertEquals(2, result.getTotalOrderCount());
        assertEquals(new BigDecimal("300.00"), result.getTotalServiceAmount());
        assertEquals(new BigDecimal("180.00"), result.getTotalSubsidyAmount());
        assertEquals(new BigDecimal("120.00"), result.getTotalSelfPayAmount());
    }

    @Test
    @DisplayName("计算结算金额 - 排除已结算订单")
    void shouldExcludeAlreadySettledOrders() {
        SettlementCalculateDTO dto = new SettlementCalculateDTO();
        dto.setProviderId("P001");

        Settlement existingSettlement = new Settlement();
        existingSettlement.setOrderId("ORD001");
        when(settlementMapper.selectList(any())).thenReturn(List.of(existingSettlement));

        Order order1 = createOrder("ORD001", new BigDecimal("100.00"),
            new BigDecimal("60.00"), new BigDecimal("40.00"));
        Order order2 = createOrder("ORD002", new BigDecimal("200.00"),
            new BigDecimal("120.00"), new BigDecimal("80.00"));

        when(orderMapper.selectList(any())).thenReturn(Arrays.asList(order1, order2));

        SettlementCalculateVO result = settlementService.calculateSettlement(dto);

        assertEquals(1, result.getTotalOrderCount());
        assertEquals(new BigDecimal("200.00"), result.getTotalServiceAmount());
        assertEquals(new BigDecimal("120.00"), result.getTotalSubsidyAmount());
        assertEquals(new BigDecimal("80.00"), result.getTotalSelfPayAmount());
    }

    @Test
    @DisplayName("计算结算金额 - 无可结算订单")
    void shouldReturnZeroWhenNoOrders() {
        SettlementCalculateDTO dto = new SettlementCalculateDTO();
        dto.setProviderId("P001");

        when(settlementMapper.selectList(any())).thenReturn(Collections.emptyList());
        when(orderMapper.selectList(any())).thenReturn(Collections.emptyList());

        SettlementCalculateVO result = settlementService.calculateSettlement(dto);

        assertEquals(0, result.getTotalOrderCount());
        assertEquals(BigDecimal.ZERO, result.getTotalServiceAmount());
        assertEquals(BigDecimal.ZERO, result.getTotalSubsidyAmount());
        assertEquals(BigDecimal.ZERO, result.getTotalSelfPayAmount());
    }

    @Test
    @DisplayName("计算结算金额 - null金额字段不影响汇总")
    void shouldHandleNullAmounts() {
        SettlementCalculateDTO dto = new SettlementCalculateDTO();
        dto.setProviderId("P001");

        when(settlementMapper.selectList(any())).thenReturn(Collections.emptyList());

        Order order1 = createOrder("ORD001", new BigDecimal("100.00"), null, null);
        Order order2 = createOrder("ORD002", null, new BigDecimal("50.00"), null);

        when(orderMapper.selectList(any())).thenReturn(Arrays.asList(order1, order2));

        SettlementCalculateVO result = settlementService.calculateSettlement(dto);

        assertEquals(2, result.getTotalOrderCount());
        assertEquals(new BigDecimal("100.00"), result.getTotalServiceAmount());
        assertEquals(new BigDecimal("50.00"), result.getTotalSubsidyAmount());
        assertEquals(BigDecimal.ZERO, result.getTotalSelfPayAmount());
    }

    private Order createOrder(String orderId, BigDecimal estimatedPrice,
                              BigDecimal subsidyAmount, BigDecimal selfPayAmount) {
        Order order = new Order();
        order.setOrderId(orderId);
        order.setStatus("COMPLETED");
        order.setEstimatedPrice(estimatedPrice);
        order.setSubsidyAmount(subsidyAmount);
        order.setSelfPayAmount(selfPayAmount);
        return order;
    }
}
