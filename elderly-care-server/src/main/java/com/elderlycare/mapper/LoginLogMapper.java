package com.elderlycare.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.elderlycare.entity.LoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;

/**
 * 登录日志Mapper接口
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {

    /**
     * 记录登录日志
     */
    int insertLoginLog(LoginLog loginLog);

    /**
     * 更新登录日志
     */
    int updateLoginLog(@Param("loginLogId") String loginLogId, @Param("loginStatus") String loginStatus, @Param("failReason") String failReason);

    /**
     * 查询用户的最近登录日志
     */
    LoginLog selectLatestByUserId(@Param("userId") String userId);

    /**
     * 统计用户在指定时间后的登录失败次数
     */
    int countLoginFailByUserIdAfter(@Param("userId") String userId, @Param("time") LocalDateTime time);
}
