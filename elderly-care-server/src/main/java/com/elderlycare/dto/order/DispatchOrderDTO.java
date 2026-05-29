package com.elderlycare.dto.order;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 订单派单DTO
 */
@Data
public class DispatchOrderDTO implements Serializable {

    @NotBlank(message = "服务商不能为空")
    private String providerId;

    @NotBlank(message = "服务人员不能为空")
    private String staffId;

    private String dispatchType;
}
