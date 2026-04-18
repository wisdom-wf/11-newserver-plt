package com.elderlycare.common;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 汉字转拼音首字母工具类
 */
public class ChineseToPinyin {

    private static final Pattern CHINESE_PATTERN = Pattern.compile("[\\u4e00-\\u9fa5]");

    /**
     * 将汉字转换为拼音首字母缩写
     * 例如："延安宁峰养老服务有限公司" -> "YANFLYFWS"
     *
     * @param chinese 汉字字符串
     * @return 拼音首字母缩写（大写）
     */
    public static String toPinyinAbbreviation(String chinese) {
        if (chinese == null || chinese.isEmpty()) {
            return "";
        }

        StringBuilder abbreviation = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);

        Matcher matcher = CHINESE_PATTERN.matcher(chinese);
        while (matcher.find()) {
            String character = matcher.group();
            try {
                String[] pinyins = PinyinHelper.toHanyuPinyinStringArray(character.charAt(0), format);
                if (pinyins != null && pinyins.length > 0) {
                    abbreviation.append(pinyins[0].charAt(0));
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                // 忽略无法转换的字符
            }
        }

        return abbreviation.toString().toUpperCase();
    }

    /**
     * 将字符串转换为5位拼音缩写，不足5位用序号补足
     *
     * 规则：
     * - 拼音缩写 >= 5字符：取前5字符
     * - 拼音缩写 < 5字符：用序号补足（序号范围1-99）
     *
     * 例如：
     * - "阳光养老" + seq=1 -> "YGL01" (3位拼音 + 2位序号)
     * - "阳光养老" + seq=10 -> "YGL10" (3位拼音 + 2位序号)
     * - "延安宁峰养老服务有限公司" -> "YANFL" (直接截断)
     *
     * @param chinese 汉字字符串
     * @param seq     序号（1-99）
     * @return 5位账号缩写
     */
    public static String to5CharAccount(String chinese, int seq) {
        String pinyin = toPinyinAbbreviation(chinese);
        String prefix;
        int suffixLen;

        if (pinyin.length() >= 5) {
            // 拼音足够长，但seq>1时仍需追加后缀以区分同名服务商
            if (seq == 1) {
                return pinyin.substring(0, 5);
            }
            // seq > 1：使用前4位 + 序号
            prefix = pinyin.substring(0, 4);
            suffixLen = (seq >= 10) ? 2 : 1;
        } else {
            // 拼音不足5位，用序号补足
            prefix = pinyin;
            // 序号最多2位(99)，所以最多占用2位
            // 如果序号>=10，用2位；否则用1位
            suffixLen = (seq >= 10) ? 2 : 1;
        }

        // 计算前缀可用长度
        int prefixLen = 5 - suffixLen;
        if (prefix.length() > prefixLen) {
            prefix = prefix.substring(0, prefixLen);
        }

        // 构建结果
        String suffix;
        if (suffixLen == 2) {
            suffix = String.format("%02d", seq);
        } else {
            suffix = String.valueOf(seq);
        }

        return prefix + suffix;
    }
}
