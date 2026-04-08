package com.elderlycare.entity.staff;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 服务人员资质实体
 */
@Data
@TableName("t_staff_qualification")
public class StaffQualification implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String qualificationId;

    /**
     * 服务人员ID
     */
    private String staffId;

    /**
     * 资质类型：0-身份证，1-健康证，2-职业资格证，3-培训证书，4-无犯罪记录证明，5-其他
     */
    private Integer qualificationType;

    /**
     * 资质名称
     */
    private String qualificationName;

    /**
     * 资质编号
     */
    private String qualificationNo;

    /**
     * 发证机关
     */
    private String issuingAuthority;

    /**
     * 发证日期
     */
    private LocalDate issueDate;

    /**
     * 有效期至
     */
    private LocalDate expireDate;

    /**
     * 资质证书图片URL，多个用逗号分隔
     */
    private String certificateUrls;

    /**
     * 状态：0-有效，1-即将到期，2-已过期
     */
    private Integer status;

    /**
     * 删除标记：0-未删除，1-已删除
     */
    @TableLogic(value = "1", delval = "0")
    private Integer deleted;

    /**
     * 创建者ID
     */
    private Long createBy;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新者ID
     */
    private Long updateBy;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 备注
     */
    private String remark;
}
