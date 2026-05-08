package com.elderlycare.mapper.ess;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.ess.EssContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EssContractMapper extends BaseMapper<EssContract> {

    /**
     * 根据订单ID查询合同（取最新一条）
     */
    EssContract selectByOrderId(@Param("orderId") String orderId);
}
