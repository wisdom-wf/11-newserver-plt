package com.elderlycare.service.impl;

import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.common.JwtUtil;
import com.elderlycare.dto.LoginDTO;
import com.elderlycare.dto.PasswordDTO;
import com.elderlycare.entity.LoginLog;
import com.elderlycare.entity.User;
import com.elderlycare.mapper.LoginLogMapper;
import com.elderlycare.mapper.UserMapper;
import com.elderlycare.service.AuthService;
import com.elderlycare.service.UserService;
import com.elderlycare.vo.LoginVO;
import com.elderlycare.vo.UserInfoVO;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private LoginLogMapper loginLogMapper;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private Long expiration;

    /** 密码最大失败次数 */
    private static final int MAX_LOGIN_FAIL_COUNT = 5;

    /** 锁定时间(分钟) */
    private static final int LOCK_MINUTES = 30;

    /** BCrypt加密因子 */
    private static final String BCRYPT_SALT = "$2a$10$";

    /** AES加密密钥 */
    private static final String AES_KEY = "ElderlyCarePwdKey2026";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO loginDTO, String loginIp) {
        String username = loginDTO.getUsername();
        String password = loginDTO.getPassword();

        // 查询用户
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new BusinessException(401, "用户名或密码错误");
        }

        // 检查用户状态
        if (!"NORMAL".equals(user.getStatus())) {
            throw new BusinessException(401, "用户已被禁用");
        }

        // 检查是否被锁定
        if (user.getLockTime() != null) {
            LocalDateTime unlockTime = user.getLockTime().plusMinutes(LOCK_MINUTES);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                long minutes = java.time.Duration.between(LocalDateTime.now(), unlockTime).toMinutes();
                throw new BusinessException(401, "用户已被锁定，请" + minutes + "分钟后再试");
            } else {
                // 解锁用户
                userMapper.unlockUser(user.getUserId());
            }
        }

        // 验证密码
        if (!verifyPassword(password, user.getPassword())) {
            // 增加登录失败次数
            userMapper.incrementLoginFailCount(user.getUserId());
            int failCount = (user.getLoginFailCount() == null ? 0 : user.getLoginFailCount()) + 1;

            // 记录登录日志
            recordLoginLog(user.getUserId(), username, loginIp, "FAIL", "密码错误");

            if (failCount >= MAX_LOGIN_FAIL_COUNT) {
                // 锁定用户
                userMapper.lockUser(user.getUserId());
                recordLoginLog(user.getUserId(), username, loginIp, "LOCK", "连续登录失败" + MAX_LOGIN_FAIL_COUNT + "次");
                throw new BusinessException(401, "连续登录失败" + MAX_LOGIN_FAIL_COUNT + "次，用户已被锁定" + LOCK_MINUTES + "分钟");
            }

            throw new BusinessException(401, "用户名或密码错误，剩余尝试次数：" + (MAX_LOGIN_FAIL_COUNT - failCount));
        }

        // 登录成功
        // 更新登录信息
        userMapper.updateLoginInfo(user.getUserId(), loginIp);

        // 记录登录日志
        recordLoginLog(user.getUserId(), username, loginIp, "SUCCESS", null);

        // 生成Token
        String accessToken = jwtUtil.generateToken(user.getUserId(), user.getUsername());

        // 构建响应
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setTokenType("Bearer");
        loginVO.setExpiresIn(expiration / 1000);
        loginVO.setUserInfo(getCurrentUserInfo(user.getUserId()));

        return loginVO;
    }

    @Override
    public void logout(String userId) {
        // 可以在这里添加退出逻辑，如将Token加入黑名单
    }

    @Override
    public UserInfoVO getCurrentUserInfo(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        UserInfoVO userInfo = new UserInfoVO();
        userInfo.setUserId(user.getUserId());
        userInfo.setUserName(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setGender(user.getGender());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setUserType(user.getUserType());
        userInfo.setTenantId(user.getTenantId());
        userInfo.setAreaId(user.getAreaId());
        userInfo.setProviderId(user.getProviderId());
        userInfo.setRoles(userService.getUserRoleCodes(userId));
        // buttons 用于前端 hasAuth() 按钮级权限检查
        userInfo.setButtons(userService.getUserPermissionCodes(userId));

        return userInfo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(String userId, PasswordDTO passwordDTO) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 验证旧密码
        if (!verifyPassword(passwordDTO.getOldPassword(), user.getPassword())) {
            throw new BusinessException(400, "旧密码错误");
        }

        // 验证新密码和确认密码
        if (!passwordDTO.getNewPassword().equals(passwordDTO.getConfirmPassword())) {
            throw new BusinessException(400, "新密码和确认密码不一致");
        }

        // 更新密码
        String encodedPassword = encodePassword(passwordDTO.getNewPassword());
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setPassword(encodedPassword);
        userMapper.updateById(updateUser);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(String userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        String encodedPassword = encodePassword(newPassword);
        User updateUser = new User();
        updateUser.setUserId(userId);
        updateUser.setPassword(encodedPassword);
        userMapper.updateById(updateUser);
    }

    /**
     * 记录登录日志
     */
    private void recordLoginLog(String userId, String username, String loginIp, String status, String failReason) {
        LoginLog loginLog = new LoginLog();
        loginLog.setLoginLogId(IDGenerator.generateId());
        loginLog.setUserId(userId);
        loginLog.setUsername(username);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setLoginIp(loginIp);
        loginLog.setLoginStatus(status);
        loginLog.setFailReason(failReason);
        loginLogMapper.insert(loginLog);
    }

    /**
     * 密码加密（使用AES）
     */
    private String encodePassword(String password) {
        // 使用Hutool的SymmetricCrypto进行AES加密
        byte[] key = AES_KEY.getBytes();
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key);
        byte[] encrypted = aes.encrypt(password);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    /**
     * 密码验证（支持BCrypt和明文）
     */
    private boolean verifyPassword(String rawPassword, String encodedPassword) {
        // 优先使用BCrypt验证
        if (encodedPassword != null && encodedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, encodedPassword);
        }
        // 兼容明文密码
        return rawPassword.equals(encodedPassword);
    }
}
