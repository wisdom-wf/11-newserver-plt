package com.elderlycare.vo.appointment;

import lombok.Data;

/**
 * 公开预约Token验证VO
 */
@Data
public class PublicAppointmentVO {

    /** token是否有效 */
    private boolean valid;

    /** 失效原因（valid=false时） */
    private String errorMsg;
}
