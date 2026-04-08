package com.elderlycare.mapper.provider;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.provider.ProviderQueryDTO;
import com.elderlycare.entity.provider.Provider;
import org.apache.ibatis.annotations.Param;

/**
 * 服务商Mapper接口
 */
public interface ProviderMapper extends BaseMapper<Provider> {

    /**
     * 分页查询服务商
     */
    IPage<Provider> selectProviderPage(Page<?> page, @Param("query") ProviderQueryDTO query);
}
