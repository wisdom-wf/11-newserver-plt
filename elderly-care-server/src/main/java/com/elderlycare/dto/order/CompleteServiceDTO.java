package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;
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
}
