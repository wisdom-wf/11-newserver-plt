package com.elderlycare.service.provider;

import com.baomidou.mybatisplus.extension.service.IService;
import com.elderlycare.dto.provider.QualificationCreateDTO;
import com.elderlycare.entity.provider.ProviderQualification;
import com.elderlycare.vo.provider.QualificationVO;

import java.util.List;

/**
 * 服务商资质Service接口
 */
public interface ProviderQualificationService extends IService<ProviderQualification> {

    /**
     * 创建资质
     */
    String createQualification(String providerId, QualificationCreateDTO dto);

    /**
     * 获取资质列表
     */
    List<QualificationVO> getQualificationsByProviderId(String providerId);

    /**
     * 删除资质
     */
    void deleteQualification(String certId);

    /**
     * 检查资质是否属于指定服务商
     */
    boolean isQualificationOwnedByProvider(String certId, String providerId);
}
