package com.elderlycare.entity.ess;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_ess_contract")
public class EssContract {

    @TableId
    private String contractId;

    private String contractNo;

    private String orderId;

    private String orderNo;

    private String flowId;

    private String contractName;

    private String signers;

    private String status;

    private String signUrl;

    private LocalDateTime signedTime;

    private String downloadUrl;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}