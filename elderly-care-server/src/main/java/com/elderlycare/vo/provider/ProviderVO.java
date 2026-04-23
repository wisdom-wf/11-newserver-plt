package com.elderlycare.vo.provider;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务商视图对象
 */
@Data
public class ProviderVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String providerId;
    private String providerName;
    private String providerType;
    private String serviceCategory;
    private String creditCode;
    private String legalPerson;
    private String contactPhone;
    private String address;
    private String serviceAreas;
    private String description;
    private String status;
    private Double rating;
    private Integer ratingCount;

    /**
     * 营业执照(base64)
     */
    private String businessLicense;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /**
     * 资质列表
     */
    private List<QualificationVO> qualifications;

    /**
     * 服务类型列表
     */
    private List<ServiceTypeVO> serviceTypes;
}
