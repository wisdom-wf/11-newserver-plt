package com.elderlycare.service.order;

import com.elderlycare.common.BusinessException;
import com.elderlycare.dto.order.CancelOrderDTO;
import com.elderlycare.dto.order.CompleteServiceDTO;
import com.elderlycare.dto.order.DispatchOrderDTO;
import com.elderlycare.dto.order.StartServiceDTO;
import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.order.OrderDispatch;
import com.elderlycare.entity.order.OrderStatus;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.entity.staff.Staff;
import com.elderlycare.mapper.appointment.AppointmentMapper;
import com.elderlycare.mapper.config.ConfigServiceTypeMapper;
import com.elderlycare.mapper.order.OrderDispatchMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.order.ServiceRecordMapper;
import com.elderlycare.mapper.provider.ProviderMapper;
import com.elderlycare.mapper.quality.QualityCheckMapper;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.mapper.staff.StaffMapper;
import com.elderlycare.service.ess.ContractService;
import com.elderlycare.service.order.impl.OrderServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("订单状态机测试")
class OrderStateMachineTest {

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock private OrderMapper orderMapper;
    @Mock private OrderDispatchMapper orderDispatchMapper;
    @Mock private AppointmentMapper appointmentMapper;
    @Mock private ProviderMapper providerMapper;
    @Mock private StaffMapper staffMapper;
    @Mock private ServiceRecordMapper serviceRecordMapper;
    @Mock private ServiceLogMapper serviceLogMapper;
    @Mock private QualityCheckMapper qualityCheckMapper;
    @Mock private ConfigServiceTypeMapper configServiceTypeMapper;
    @Mock private ContractService contractService;

    private Order order;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setOrderId("ORD001");
        order.setOrderNo("202401010001");
        order.setElderName("张三");
        order.setProviderId("P001");
    }

    @Nested
    @DisplayName("派单 - dispatchOrder")
    class DispatchTests {

        @Test
        @DisplayName("待派单状态可以派单")
        void shouldDispatchWhenCreated() {
            order.setStatus(OrderStatus.CREATED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);
            Provider provider = new Provider();
            provider.setStatus("ENABLED");
            when(providerMapper.selectById("P001")).thenReturn(provider);
            when(orderDispatchMapper.insert(any())).thenReturn(1);
            when(orderMapper.updateById(any())).thenReturn(1);

            DispatchOrderDTO dto = new DispatchOrderDTO();
            dto.setProviderId("P001");

            assertDoesNotThrow(() -> orderService.dispatchOrder("ORD001", dto));
            verify(orderMapper).updateById(argThat(o ->
                OrderStatus.DISPATCHED.getCode().equals(((Order) o).getStatus())
            ));
        }

        @Test
        @DisplayName("非待派单状态不能派单")
        void shouldRejectDispatchWhenNotCreated() {
            order.setStatus(OrderStatus.DISPATCHED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);

            DispatchOrderDTO dto = new DispatchOrderDTO();
            dto.setProviderId("P001");

            BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.dispatchOrder("ORD001", dto));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("禁用的服务商不能派单")
        void shouldRejectDispatchToDisabledProvider() {
            order.setStatus(OrderStatus.CREATED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);
            Provider provider = new Provider();
            provider.setStatus("DISABLED");
            when(providerMapper.selectById("P001")).thenReturn(provider);

            DispatchOrderDTO dto = new DispatchOrderDTO();
            dto.setProviderId("P001");

            BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.dispatchOrder("ORD001", dto));
            assertTrue(ex.getMessage().contains("禁用"));
        }

        @Test
        @DisplayName("离职人员不能接单")
        void shouldRejectDispatchToOffJobStaff() {
            order.setStatus(OrderStatus.CREATED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);
            Provider provider = new Provider();
            provider.setStatus("ENABLED");
            when(providerMapper.selectById("P001")).thenReturn(provider);
            Staff staff = new Staff();
            staff.setStatus("OFF_JOB");
            when(staffMapper.selectStaffById("S001")).thenReturn(staff);

            DispatchOrderDTO dto = new DispatchOrderDTO();
            dto.setProviderId("P001");
            dto.setStaffId("S001");

            BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.dispatchOrder("ORD001", dto));
            assertTrue(ex.getMessage().contains("在职"));
        }
    }

    @Nested
    @DisplayName("取消订单 - cancelOrder")
    class CancelTests {

        @Test
        @DisplayName("待派单状态可以取消")
        void shouldCancelWhenCreated() {
            order.setStatus(OrderStatus.CREATED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);
            when(orderMapper.updateById(any())).thenReturn(1);

            CancelOrderDTO dto = new CancelOrderDTO();
            dto.setCancelReason("客户取消");

            assertDoesNotThrow(() -> orderService.cancelOrder("ORD001", dto));
            verify(orderMapper).updateById(argThat(o ->
                OrderStatus.CANCELLED.getCode().equals(((Order) o).getStatus())
            ));
        }

        @Test
        @DisplayName("已完成订单不能取消")
        void shouldRejectCancelWhenCompleted() {
            order.setStatus(OrderStatus.SERVICE_COMPLETED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);

            CancelOrderDTO dto = new CancelOrderDTO();
            dto.setCancelReason("客户取消");

            BusinessException ex = assertThrows(BusinessException.class,
                () -> orderService.cancelOrder("ORD001", dto));
            assertEquals(400, ex.getCode());
        }

        @Test
        @DisplayName("已结算订单不能取消")
        void shouldRejectCancelWhenSettled() {
            order.setStatus(OrderStatus.SETTLED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);

            CancelOrderDTO dto = new CancelOrderDTO();
            dto.setCancelReason("客户取消");

            assertThrows(BusinessException.class,
                () -> orderService.cancelOrder("ORD001", dto));
        }

        @Test
        @DisplayName("服务中状态可以取消")
        void shouldCancelWhenInService() {
            order.setStatus(OrderStatus.SERVICE_STARTED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);
            when(orderMapper.updateById(any())).thenReturn(1);

            CancelOrderDTO dto = new CancelOrderDTO();
            dto.setCancelReason("紧急取消");

            assertDoesNotThrow(() -> orderService.cancelOrder("ORD001", dto));
        }
    }

    @Nested
    @DisplayName("开始服务 - startService")
    class StartServiceTests {

        @Test
        @DisplayName("已接单状态可以开始服务")
        void shouldStartWhenReceived() {
            order.setStatus(OrderStatus.RECEIVED.getCode());
            when(orderMapper.selectByIdWithNames("ORD001")).thenReturn(order);
            when(contractService.isContractSigned("ORD001")).thenReturn(true);
            when(orderMapper.updateById(any())).thenReturn(1);

            StartServiceDTO dto = new StartServiceDTO();

            assertDoesNotThrow(() -> orderService.startService("ORD001", dto));
            verify(orderMapper).updateById(argThat(o ->
                OrderStatus.SERVICE_STARTED.getCode().equals(((Order) o).getStatus())
            ));
        }

        @Test
        @DisplayName("非已接单状态不能开始服务")
        void shouldRejectStartWhenNotReceived() {
            order.setStatus(OrderStatus.CREATED.getCode());
            when(orderMapper.selectByIdWithNames("ORD001")).thenReturn(order);

            StartServiceDTO dto = new StartServiceDTO();

            assertThrows(BusinessException.class,
                () -> orderService.startService("ORD001", dto));
        }
    }

    @Nested
    @DisplayName("完成服务 - completeService")
    class CompleteServiceTests {

        @Test
        @DisplayName("服务中状态可以完成")
        void shouldCompleteWhenStarted() {
            order.setStatus(OrderStatus.SERVICE_STARTED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);
            when(orderMapper.updateById(any())).thenReturn(1);

            CompleteServiceDTO dto = new CompleteServiceDTO();

            assertDoesNotThrow(() -> orderService.completeService("ORD001", dto));
            verify(orderMapper).updateById(argThat(o ->
                OrderStatus.SERVICE_COMPLETED.getCode().equals(((Order) o).getStatus())
            ));
        }

        @Test
        @DisplayName("非服务中状态不能完成")
        void shouldRejectCompleteWhenNotStarted() {
            order.setStatus(OrderStatus.DISPATCHED.getCode());
            when(orderMapper.selectById("ORD001")).thenReturn(order);

            CompleteServiceDTO dto = new CompleteServiceDTO();

            assertThrows(BusinessException.class,
                () -> orderService.completeService("ORD001", dto));
        }
    }
}
