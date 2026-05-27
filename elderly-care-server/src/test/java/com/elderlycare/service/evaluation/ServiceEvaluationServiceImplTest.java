package com.elderlycare.service.evaluation;

import com.elderlycare.dto.evaluation.CreateEvaluationDTO;
import com.elderlycare.dto.evaluation.EvaluationStatisticsDTO;
import com.elderlycare.entity.evaluation.ServiceEvaluation;
import com.elderlycare.entity.order.Order;
import com.elderlycare.mapper.evaluation.ServiceEvaluationMapper;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.service.evaluation.impl.ServiceEvaluationServiceImpl;
import com.elderlycare.vo.evaluation.EvaluationStatisticsVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceEvaluationServiceImplTest {

    @InjectMocks
    private ServiceEvaluationServiceImpl service;

    @Mock
    private ServiceEvaluationMapper evaluationMapper;

    @Mock
    private OrderMapper orderMapper;

    @Test
    void createEvaluationUsesOrderOwnershipFields() {
        Order order = new Order();
        order.setOrderId("O1");
        order.setProviderId("P1");
        order.setElderId("E1");
        order.setStaffId("S1");
        when(orderMapper.selectById("O1")).thenReturn(order);
        when(evaluationMapper.selectCount(any())).thenReturn(0L);

        CreateEvaluationDTO dto = new CreateEvaluationDTO();
        dto.setOrderId("O1");
        dto.setStaffId("OTHER_STAFF");
        dto.setRating(5);

        service.createEvaluation(dto);

        ArgumentCaptor<ServiceEvaluation> captor = ArgumentCaptor.forClass(ServiceEvaluation.class);
        verify(evaluationMapper).insert(captor.capture());
        ServiceEvaluation inserted = captor.getValue();
        assertEquals("P1", inserted.getProviderId());
        assertEquals("E1", inserted.getElderId());
        assertEquals("S1", inserted.getStaffId());
    }

    @Test
    void statisticsKeepsAverageNullWhenNoEvaluationsExist() {
        when(evaluationMapper.selectList(any())).thenReturn(Collections.emptyList());

        EvaluationStatisticsVO result = service.getStatistics(new EvaluationStatisticsDTO());

        assertEquals(0L, result.getTotalCount());
        assertNull(result.getAverageRating());
    }
}
