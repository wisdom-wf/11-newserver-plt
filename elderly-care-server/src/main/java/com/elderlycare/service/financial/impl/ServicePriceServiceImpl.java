package com.elderlycare.service.financial.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.financial.ServicePriceCreateDTO;
import com.elderlycare.dto.financial.ServicePriceQueryDTO;
import com.elderlycare.dto.financial.ServicePriceUpdateDTO;
import com.elderlycare.entity.financial.ServicePrice;
import com.elderlycare.mapper.financial.ServicePriceMapper;
import com.elderlycare.service.financial.ServicePriceService;
import com.elderlycare.vo.financial.ServicePriceVO;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务定价Service实现
 */
@Service
@RequiredArgsConstructor
public class ServicePriceServiceImpl extends ServiceImpl<ServicePriceMapper, ServicePrice> implements ServicePriceService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createPrice(ServicePriceCreateDTO dto) {
        ServicePrice price = new ServicePrice();
        BeanUtils.copyProperties(dto, price);
        price.setPriceId(IDGenerator.generateId());
        price.setStatus("ACTIVE");
        price.setCreateTime(LocalDateTime.now());
        price.setUpdateTime(LocalDateTime.now());

        baseMapper.insert(price);
        return price.getPriceId();
    }

    @Override
    public PageResult<ServicePriceVO> queryPrices(ServicePriceQueryDTO dto) {
        Page<ServicePrice> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<ServicePrice> result = baseMapper.selectPricePage(page, dto);

        List<ServicePrice> records = result.getRecords();
        long total = result.getTotal();

        List<ServicePriceVO> voList = records.stream().map(this::convertToVO).collect(Collectors.toList());

        return PageResult.of(total, dto.getPage(), dto.getPageSize(), voList);
    }

    @Override
    public ServicePriceVO getPriceById(String priceId) {
        ServicePrice price = baseMapper.selectById(priceId);
        if (price == null) {
            throw BusinessException.notFound("服务定价不存在");
        }
        return convertToVO(price);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePrice(String priceId, ServicePriceUpdateDTO dto) {
        ServicePrice price = baseMapper.selectById(priceId);
        if (price == null) {
            throw BusinessException.notFound("服务定价不存在");
        }

        if (StringUtils.isNotBlank(dto.getServiceTypeCode())) {
            price.setServiceTypeCode(dto.getServiceTypeCode());
        }
        if (StringUtils.isNotBlank(dto.getServiceTypeName())) {
            price.setServiceTypeName(dto.getServiceTypeName());
        }
        if (dto.getGovernmentPrice() != null) {
            price.setGovernmentPrice(dto.getGovernmentPrice());
        }
        if (dto.getSelfPayPrice() != null) {
            price.setSelfPayPrice(dto.getSelfPayPrice());
        }
        if (dto.getTotalPrice() != null) {
            price.setTotalPrice(dto.getTotalPrice());
        }
        if (StringUtils.isNotBlank(dto.getPriceType())) {
            price.setPriceType(dto.getPriceType());
        }
        if (dto.getEffectiveDate() != null) {
            price.setEffectiveDate(dto.getEffectiveDate());
        }
        if (dto.getExpiryDate() != null) {
            price.setExpiryDate(dto.getExpiryDate());
        }
        if (StringUtils.isNotBlank(dto.getStatus())) {
            price.setStatus(dto.getStatus());
        }
        if (dto.getRemark() != null) {
            price.setRemark(dto.getRemark());
        }
        price.setUpdateTime(LocalDateTime.now());

        baseMapper.updateById(price);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePrice(String priceId) {
        ServicePrice price = baseMapper.selectById(priceId);
        if (price == null) {
            throw BusinessException.notFound("服务定价不存在");
        }
        baseMapper.deleteById(priceId);
    }

    private ServicePriceVO convertToVO(ServicePrice price) {
        ServicePriceVO vo = new ServicePriceVO();
        BeanUtils.copyProperties(price, vo);
        vo.setStatusName(getStatusName(price.getStatus()));
        return vo;
    }

    private String getStatusName(String status) {
        if ("ACTIVE".equals(status)) {
            return "有效";
        } else if ("INACTIVE".equals(status)) {
            return "无效";
        } else if ("EXPIRED".equals(status)) {
            return "已过期";
        }
        return status;
    }
}
