package com.elderlycare.service.config.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.PageResult;
import com.elderlycare.dto.config.OperationLogQueryDTO;
import com.elderlycare.entity.config.OperationLog;
import com.elderlycare.mapper.config.OperationLogMapper;
import com.elderlycare.service.config.OperationLogService;
import com.elderlycare.vo.config.OperationLogVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 操作日志Service实现
 */
@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog>
        implements OperationLogService {

    @Override
    public PageResult<OperationLog> queryOperationLogs(OperationLogQueryDTO dto) {
        Page<OperationLog> page = new Page<>(dto.getPage(), dto.getPageSize());
        IPage<OperationLog> result = baseMapper.selectOperationLogPage(page, dto);

        List<OperationLog> records = result.getRecords();
        long total = result.getTotal();

        return PageResult.of(total, dto.getPage(), dto.getPageSize(), records);
    }

    @Override
    public void createOperationLog(OperationLog operationLog) {
        baseMapper.insert(operationLog);
    }

    @Override
    public OperationLogVO getOperationLogById(String operationLogId) {
        OperationLog operationLog = baseMapper.selectById(operationLogId);
        if (operationLog == null) {
            throw BusinessException.notFound("操作日志不存在");
        }

        OperationLogVO vo = new OperationLogVO();
        BeanUtils.copyProperties(operationLog, vo);
        return vo;
    }
}
