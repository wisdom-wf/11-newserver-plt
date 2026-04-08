package com.elderlycare.common;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 */
public class DateUtil {

    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";

    /**
     * 格式化日期
     */
    public static String format(Date date, String pattern) {
        if (date == null) {
            return null;
        }
        return cn.hutool.core.date.DateUtil.format(date, pattern);
    }

    /**
     * 格式化日期为 yyyy-MM-dd
     */
    public static String formatDate(Date date) {
        return format(date, YYYY_MM_DD);
    }

    /**
     * 格式化日期为 yyyy-MM-dd HH:mm:ss
     */
    public static String formatDateTime(Date date) {
        return format(date, YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * 解析日期字符串
     */
    public static Date parse(String dateStr, String pattern) {
        return cn.hutool.core.date.DateUtil.parse(dateStr, pattern);
    }

    /**
     * 获取当前日期字符串
     */
    public static String getCurrentDate() {
        return format(new Date(), YYYYMMDD);
    }

    /**
     * 获取当前时间字符串
     */
    public static String getCurrentDateTime() {
        return format(new Date(), YYYYMMDDHHMMSS);
    }

    /**
     * 生成订单号
     */
    public static String generateOrderNo() {
        return "O" + getCurrentDateTime() + IDGenerator.generateNumId(6);
    }

    /**
     * 生成编号
     */
    public static String generateNo(String prefix) {
        return prefix + getCurrentDateTime() + IDGenerator.generateNumId(4);
    }
}
