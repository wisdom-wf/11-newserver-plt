package com.elderlycare.entity.order;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单派单记录实体
 */
@Data
@TableName("t_order_dispatch")
public class OrderDispatch implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String dispatchId;

    private String orderId;

    private String providerId;

    private String staffId;

    private LocalDateTime dispatchTime;

    private String dispatcherId;

    private String dispatchType;

    private String dispatchStatus;

    private LocalDateTime receiveTime;

    private String receiveComment;

    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
