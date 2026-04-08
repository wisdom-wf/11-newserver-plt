package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;

/**
 * 取消订单DTO
 */
@Data
public class CancelOrderDTO implements Serializable {

    private String cancelReason;
}
