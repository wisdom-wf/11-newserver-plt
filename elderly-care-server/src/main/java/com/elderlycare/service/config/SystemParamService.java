package com.elderlycare.service.config;

import com.elderlycare.dto.config.SystemParamDTO;
import com.elderlycare.entity.config.SystemParam;
import com.elderlycare.vo.config.SystemParamVO;
import java.util.List;

/**
 * 系统参数Service接口
 */
public interface SystemParamService {

    /**
     * 参数列表
     */
    List<SystemParam> listParams();

    /**
     * 参数详情
     */
    SystemParamVO getParamByCode(String paramCode);

    /**
     * 参数修改
     */
    void updateParam(String paramCode, SystemParamDTO dto);
}
