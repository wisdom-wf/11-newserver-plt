package com.elderlycare.mapper.elder;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.elder.ElderPageDTO;
import com.elderlycare.entity.elder.Elder;
import com.elderlycare.vo.elder.ElderHealthCardVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 老人档案Mapper
 */
@Mapper
public interface ElderMapper extends BaseMapper<Elder> {

    /**
     * 分页查询老人档案
     */
    IPage<Elder> selectElderPage(Page<Elder> page, @Param("dto") ElderPageDTO dto);

    /**
     * 根据身份证号查询老人档案
     */
    Elder selectByIdCard(@Param("idCard") String idCard);

    /**
     * 根据手机号查询老人档案
     */
    Elder selectByPhone(@Param("phone") String phone);

    /**
     * 查询最近更新的老人档案（用于健康卡片展示）
     */
    List<ElderHealthCardVO> selectRecentUpdated(@Param("providerId") String providerId, @Param("limit") int limit);
}
