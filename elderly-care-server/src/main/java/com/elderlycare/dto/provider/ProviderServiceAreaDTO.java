package com.elderlycare.dto.provider;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 服务商服务区域DTO
 */
@Data
public class ProviderServiceAreaDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 服务区域列表
     */
    private List<String> serviceAreas;
}
