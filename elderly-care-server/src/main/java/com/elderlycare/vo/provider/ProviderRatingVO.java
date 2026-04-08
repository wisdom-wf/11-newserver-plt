package com.elderlycare.vo.provider;

import lombok.Data;
import java.io.Serializable;

/**
 * 服务商评分视图对象
 */
@Data
public class ProviderRatingVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务商ID
     */
    private String providerId;

    /**
     * 服务商名称
     */
    private String providerName;

    /**
     * 平均评分
     */
    private Double averageRating;

    /**
     * 评分次数
     */
    private Integer ratingCount;

    /**
     * 5星评分数量
     */
    private Integer fiveStarCount;

    /**
     * 4星评分数量
     */
    private Integer fourStarCount;

    /**
     * 3星评分数量
     */
    private Integer threeStarCount;

    /**
     * 2星评分数量
     */
    private Integer twoStarCount;

    /**
     * 1星评分数量
     */
    private Integer oneStarCount;
}
