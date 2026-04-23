package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.PublicToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

/**
 * 公开Token Mapper接口
 */
@Mapper
public interface PublicTokenMapper extends BaseMapper<PublicToken> {

    /**
     * 根据Token值查询有效Token
     */
    PublicToken selectByToken(@Param("token") String token);

    /**
     * 撤销Token
     */
    int revokeToken(@Param("token") String token);

    /**
     * 删除过期Token
     */
    int deleteExpiredTokens(@Param("now") LocalDateTime now);
}
