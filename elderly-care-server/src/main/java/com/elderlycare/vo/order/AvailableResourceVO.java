package com.elderlycare.vo.order;

import lombok.Data;
import java.io.Serializable;

/**
 * 可派单资源VO
 */
@Data
public class AvailableResourceVO implements Serializable {

    private String providerId;

    private String providerName;

    private String providerAddress;

    private String staffId;

    private String staffName;

    private String serviceTypeName;

    private Double distance;

    private Double rating;
}
