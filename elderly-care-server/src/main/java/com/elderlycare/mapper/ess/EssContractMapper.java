package com.elderlycare.mapper.ess;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.ess.EssContract;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EssContractMapper extends BaseMapper<EssContract> {

    /**
     * 根据订单ID查询合同
     */
    EssContract selectByOrderId(@Param("orderId") String orderId);

    /**
     * 获取合同列表（带分页）
     */
    List<EssContract> selectContractList(@Param("status") String status, @Param("contractNo") String contractNo);
}