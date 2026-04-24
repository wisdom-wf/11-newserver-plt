package com.elderlycare.service.elder;

import com.elderlycare.vo.elder.CareSuggestionVO;
import com.elderlycare.vo.elder.MedicalSuggestionVO;

/**
 * 健康建议服务接口
 */
public interface HealthAdviceService {

    /**
     * 获取护理建议
     * @param elderId 老人ID
     * @return 护理建议
     */
    CareSuggestionVO getCareSuggestions(String elderId);

    /**
     * 获取就医建议
     * @param elderId 老人ID
     * @return 就医建议
     */
    MedicalSuggestionVO getMedicalSuggestions(String elderId);
}
