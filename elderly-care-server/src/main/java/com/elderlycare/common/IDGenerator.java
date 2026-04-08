package com.elderlycare.common;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;

/**
 * ID生成器
 */
public class IDGenerator {

    private static final Snowflake SNOWFLAKE = IdUtil.getSnowflake(1, 1);

    /**
     * 生成22位字符串ID
     */
    public static String generateId() {
        return String.valueOf(SNOWFLAKE.nextId());
    }

    /**
     * 生成指定位数数字ID
     */
    public static String generateNumId(int length) {
        String id = String.valueOf(SNOWFLAKE.nextId());
        if (id.length() > length) {
            return id.substring(id.length() - length);
        }
        return id;
    }
}
