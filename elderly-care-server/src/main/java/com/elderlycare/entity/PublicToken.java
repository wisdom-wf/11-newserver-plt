package com.elderlycare.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 公开访问Token实体
 */
@Data
@TableName("t_public_token")
public class PublicToken implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Token ID */
    @TableId(type = IdType.ASSIGN_ID)
    private String tokenId;

    /** Token值 */
    private String token;

    /** Token类型：cockpit-驾驶舱 */
    private String type;

    /** 过期时间 */
    private LocalDateTime expiresAt;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 创建人 */
    private String createBy;

    /** 状态：ACTIVE-有效，REVOKED-已撤销 */
    private String status;

    /** 备注 */
    private String remark;
}
