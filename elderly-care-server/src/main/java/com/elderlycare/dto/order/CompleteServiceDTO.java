package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 完成服务DTO
 */
@Data
public class CompleteServiceDTO implements Serializable {

    private String staffId;

    private String serviceSummary;

    private List<String> servicePhotos;

    private String longitude;

    private String latitude;

    /** 实际服务金额 (前端传入的字段名) */
    private BigDecimal actualFee;

    /** 实际服务金额 */
    private BigDecimal actualServiceFee;

    /** 补贴金额 */
    private BigDecimal subsidyAmount;

    /** 自付金额 */
    private BigDecimal selfPayAmount;
}
