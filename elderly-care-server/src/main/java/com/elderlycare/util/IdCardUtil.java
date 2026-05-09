package com.elderlycare.util;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;

/**
 * 身份证号码工具类
 * 18位身份证：第7-14位为出生日期，第17位为性别（奇数男，偶数女）
 */
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
     * 从身份证号计算年龄
     */
    public static Integer calculateAge(String idCard) {
        LocalDate birthDate = parseBirthDate(idCard);
        if (birthDate == null) {
            return null;
        }
        return Period.between(birthDate, LocalDate.now()).getYears();
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
