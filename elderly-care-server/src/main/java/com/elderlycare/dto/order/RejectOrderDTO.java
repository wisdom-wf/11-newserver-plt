package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单拒单DTO
 */
@Data
public class RejectOrderDTO implements Serializable {

    private String staffId;

    private String rejectReason;
}
