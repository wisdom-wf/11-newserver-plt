package com.elderlycare.dto.order;

import lombok.Data;
import java.io.Serializable;

/**
 * 开始服务DTO
 */
@Data
public class StartServiceDTO implements Serializable {

    private String staffId;

    private String longitude;

    private String latitude;

    private String checkInPhoto;
}
