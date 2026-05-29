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
     * 根据资质ID获取资质
     */
    QualificationVO getQualificationById(String qualificationId);

    /**
     * 删除资质
     */
    void deleteQualification(String qualificationId);

    /**
     * 更新资质
     */
    void updateQualification(String qualificationId, QualificationCreateDTO dto);

    /**
     * 检查资质是否属于指定服务商
     */
    boolean isQualificationOwnedByProvider(String qualificationId, String providerId);

    /**
     * 获取资质预览列表（不含图片base64）
     * 返回 HAS_IMAGES 标记代替真实图片
     */
    List<QualificationVO> getQualificationsPreviewByProviderId(String providerId);

    /**
     * 获取单个资质的真实图片URL
     */
    String getQualificationImages(String qualificationId);
}
