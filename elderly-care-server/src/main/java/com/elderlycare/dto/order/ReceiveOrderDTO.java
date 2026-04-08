package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单接单DTO
 */
@Data
public class ReceiveOrderDTO implements Serializable {

    private String staffId;

    private String receiveComment;
}
