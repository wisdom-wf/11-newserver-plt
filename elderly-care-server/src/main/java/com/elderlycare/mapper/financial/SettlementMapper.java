package com.elderlycare.mapper.financial;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.financial.SettlementQueryDTO;
import com.elderlycare.entity.financial.Settlement;
import org.apache.ibatis.annotations.Param;

/**
 * 结算单Mapper接口
 */
public interface SettlementMapper extends BaseMapper<Settlement> {

    /**
     * 分页查询结算单
     */
    IPage<Settlement> selectSettlementPage(Page<?> page, @Param("query") SettlementQueryDTO query);
}
