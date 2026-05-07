package com.elderlycare.vo.ess;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SignUrlVO {

    private String signUrl;

    private LocalDateTime expireTime;
}