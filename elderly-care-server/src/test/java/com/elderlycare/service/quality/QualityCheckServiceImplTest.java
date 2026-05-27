package com.elderlycare.service.quality;

import com.elderlycare.entity.order.Order;
import com.elderlycare.entity.quality.QualityCheck;
import com.elderlycare.mapper.order.OrderMapper;
import com.elderlycare.mapper.quality.QualityCheckMapper;
import com.elderlycare.mapper.servicelog.ServiceLogMapper;
import com.elderlycare.service.evaluation.ServiceEvaluationService;
import com.elderlycare.service.quality.impl.QualityCheckServiceImpl;
import com.elderlycare.vo.quality.QualityCheckVO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QualityCheckServiceImplTest {

    @InjectMocks
    private QualityCheckServiceImpl service;

    @Mock
    private QualityCheckMapper qualityCheckMapper;

    @Mock
    private ServiceLogMapper serviceLogMapper;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ServiceEvaluationService evaluationService;

    @Test
    void createQualityCheckUsesCanonicalOrderAssociationData() {
        Order order = new Order();
        order.setOrderId("O1");
        order.setOrderNo("ORD-001");
        order.setProviderId("P1");
        order.setProviderName("Provider One");
        order.setStaffId("S1");
        order.setStaffName("Staff One");
        when(orderMapper.selectByIdWithNames("O1")).thenReturn(order);

        QualityCheckVO request = new QualityCheckVO();
        request.setOrderId("O1");
        request.setProviderId("OTHER_PROVIDER");
        request.setStaffId("OTHER_STAFF");
        request.setCheckType("COMPLETION");
        request.setCheckMethod("PHOTO_REVIEW");
        request.setCheckResult("PENDING");

        service.createQualityCheck(request);

        ArgumentCaptor<QualityCheck> captor = ArgumentCaptor.forClass(QualityCheck.class);
        verify(qualityCheckMapper).insert(captor.capture());
        QualityCheck inserted = captor.getValue();
        assertEquals("ORD-001", inserted.getOrderNo());
        assertEquals("P1", inserted.getProviderId());
        assertEquals("S1", inserted.getStaffId());
    }
}
