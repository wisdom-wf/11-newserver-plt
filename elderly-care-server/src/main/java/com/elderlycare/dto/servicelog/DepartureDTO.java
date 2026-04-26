package com.elderlycare.dto.servicelog;

import lombok.Data;
import java.io.Serializable;

/**
 * 出发DTO
 */
@Data
public class DepartureDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 出发位置(经纬度，格式：latitude,longitude)
     */
    private String location;

    /**
     * 出发备注
     */
    private String remark;
}
