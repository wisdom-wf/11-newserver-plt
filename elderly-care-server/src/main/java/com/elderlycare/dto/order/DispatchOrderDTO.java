package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;

/**
 * 订单派单DTO
 */
@Data
public class DispatchOrderDTO implements Serializable {

    private String providerId;

    private String staffId;

    private String dispatchType;
}
