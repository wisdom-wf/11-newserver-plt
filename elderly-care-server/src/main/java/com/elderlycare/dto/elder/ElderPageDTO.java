package com.elderlycare.dto.elder;

import lombok.Data;
import java.io.Serializable;

/**
 * 老人档案分页查询DTO
 */
@Data
public class ElderPageDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer pageSize = 10;

    /**
     * 老人姓名
     */
    private String name;

    /**
     * 身份证号
     */
    private String idCard;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 状态：ACTIVE/PENDING/SUSPENDED/CANCELLED
     */
    private String status;

    /**
     * 护理等级：NORMAL/SUBSIDIZED/FULL
     */
    private String careLevel;

    /**
     * 养老类型：HOME/COMMUNITY/INSTITUTION
     */
    private String careType;

    /**
     * 服务商ID（数据权限过滤）
     */
    private String providerId;
}
