package com.elderlycare.service;

import java.util.Map;

/**
 * 验证码服务接口
 */
public interface CaptchaService {

    /**
     * 发送验证码
     * @param phone 手机号
     * @param type 验证码类型（如 PHONE_LOGIN）
     * @return 验证码Key
     */
    String sendCaptcha(String phone, String type);

    /**
     * 验证验证码
     * @param phone 手机号
     * @param type 验证码类型
     * @param code 用户输入的验证码
     * @return 是否验证通过
     */
    boolean verifyCaptcha(String phone, String type, String code);

    /**
     * 获取验证码（调试用）
     * @param phone 手机号
     * @param type 验证码类型
     * @return 验证码
     */
    String getCaptcha(String phone, String type);
}