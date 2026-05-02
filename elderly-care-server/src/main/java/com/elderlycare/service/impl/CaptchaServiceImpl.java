package com.elderlycare.service.impl;

import com.elderlycare.common.BusinessException;
import com.elderlycare.service.CaptchaService;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 验证码服务实现类（内存存储，生产环境应使用Redis）
 */
@Service
public class CaptchaServiceImpl implements CaptchaService {

    /** 验证码存储：key = phone:type, value = code */
    private final Map<String, String> captchaStore = new ConcurrentHashMap<>();

    /** 验证码有效期（秒） */
    private static final long CAPTCHA_TTL = 300;

    /** 验证码创建时间：key = phone:type, value = 创建时间戳 */
    private final Map<String, Long> captchaTimeStore = new ConcurrentHashMap<>();

    @Override
    public String sendCaptcha(String phone, String type) {
        // 生成6位验证码
        String code = String.format("%06d", (int) (Math.random() * 1000000));

        String key = buildKey(phone, type);
        captchaStore.put(key, code);
        captchaTimeStore.put(key, System.currentTimeMillis());

        // 实际应该调用短信服务发送验证码，这里仅记录日志
        System.out.println("[Captcha] 发送验证码到 " + phone + "，类型：" + type + "，验证码：" + code);

        return key;
    }

    @Override
    public boolean verifyCaptcha(String phone, String type, String code) {
        String key = buildKey(phone, type);
        String storedCode = captchaStore.get(key);

        if (storedCode == null) {
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }

        // 检查是否过期
        Long createTime = captchaTimeStore.get(key);
        if (createTime != null && System.currentTimeMillis() - createTime > CAPTCHA_TTL * 1000) {
            captchaStore.remove(key);
            captchaTimeStore.remove(key);
            throw new BusinessException(400, "验证码已过期，请重新获取");
        }

        // 验证通过后删除验证码（一次性）
        if (storedCode.equals(code)) {
            captchaStore.remove(key);
            captchaTimeStore.remove(key);
            return true;
        }

        return false;
    }

    @Override
    public String getCaptcha(String phone, String type) {
        String key = buildKey(phone, type);
        return captchaStore.get(key);
    }

    private String buildKey(String phone, String type) {
        return phone + ":" + type;
    }
}