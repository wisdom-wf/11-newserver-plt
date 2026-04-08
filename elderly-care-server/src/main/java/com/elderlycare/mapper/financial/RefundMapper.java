package com.elderlycare.mapper.financial;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.elderlycare.dto.financial.RefundQueryDTO;
import com.elderlycare.entity.financial.Refund;
import org.apache.ibatis.annotations.Param;

/**
 * 退款记录Mapper接口
 */
public interface RefundMapper extends BaseMapper<Refund> {

    /**
     * 分页查询退款记录
     */
    IPage<Refund> selectRefundPage(Page<?> page, @Param("query") RefundQueryDTO query);
}
