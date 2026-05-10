package com.elderlycare.util;

import lombok.extern.slf4j.Slf4j;
import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

@Slf4j
public class IdCardUtil {

    /**
     * 从身份证号提取出生日期
     */
    public static LocalDate parseBirthDate(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return null;
        }
        try {
            String dateStr = idCard.substring(6, 14); // YYYYMMDD
            return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern("yyyyMMdd"));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从身份证号计算年龄（按年份差）
     */
    public static Integer calculateAge(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return null;
        }
        try {
            int birthYear = Integer.parseInt(idCard.substring(6, 10));
            int currentYear = java.time.Year.now().getValue();
            return currentYear - birthYear;
        } catch (NumberFormatException e) {
            log.warn("身份证号格式异常，年份无法解析: {}", idCard);
            return null;
        }
    }

    /**
     * 从身份证号获取性别
     * @return "0"=女, "1"=男, null=无法解析
     */
    public static String parseGender(String idCard) {
        if (idCard == null || idCard.length() != 18) {
            return null;
        }
        try {
            char genderChar = idCard.charAt(16); // 第17位（0-indexed为16）
            int genderDigit = Character.getNumericValue(genderChar);
            return (genderDigit % 2 == 0) ? "0" : "1"; // 偶数女，奇数男
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取性别文本
     */
    public static String getGenderText(String gender) {
        if (gender == null) return "未知";
        return "1".equals(gender) ? "男" : "0".equals(gender) ? "女" : "未知";
    }
}
