package com.elderlycare.dto.elder;

import lombok.Data;

/**
 * 图片识别请求DTO
 */
@Data
public class RecognizeImageDTO {
    /**
     * 图片数据：URL 或 base64（data:image/...;base64,xxx）
     */
    private String imageData;
}
