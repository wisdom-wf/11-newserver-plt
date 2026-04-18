package com.elderlycare.service.provider;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.ChineseToPinyin;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.entity.User;
import com.elderlycare.entity.UserRole;
import com.elderlycare.entity.provider.Provider;
import com.elderlycare.mapper.UserMapper;
import com.elderlycare.mapper.UserRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 服务商账号自动创建服务
 * <p>
 * 每新增一个服务商，自动创建一个对应的服务商管理员账号
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProviderAccountService {

    public static final String DEFAULT_PASSWORD = "mima123";
    private static final String ROLE_ID_PROVIDER_ADMIN = "R002";
    private static final String USER_TYPE_PROVIDER = "PROVIDER";
    private static final String STATUS_NORMAL = "NORMAL";

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    /**
     * 创建服务商管理员账号
     */
    @Transactional(rollbackFor = Exception.class)
    public User createProviderAccount(Provider provider) {
        String username = generateUsername(provider.getProviderName());
        log.info("为服务商[{}]生成账号: {}", provider.getProviderName(), username);

        User user = new User();
        user.setUserId(IDGenerator.generateId());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        user.setRealName(provider.getProviderName() + "管理员");
        user.setPhone(provider.getContactPhone());
        user.setUserType(USER_TYPE_PROVIDER);
        user.setProviderId(provider.getProviderId());
        user.setStatus(STATUS_NORMAL);
        user.setCreateTime(LocalDateTime.now());
        user.setDeleted(0);

        userMapper.insert(user);

        UserRole userRole = new UserRole();
        userRole.setUserRoleId(IDGenerator.generateId());
        userRole.setUserId(user.getUserId());
        userRole.setRoleId(ROLE_ID_PROVIDER_ADMIN);
        userRole.setCreateTime(LocalDateTime.now());
        userRoleMapper.insert(userRole);

        log.info("服务商管理员账号创建成功: username={}, providerId={}", username, provider.getProviderId());
        return user;
    }

    /**
     * 生成5位用户名
     */
    private String generateUsername(String providerName) {
        String baseUsername = ChineseToPinyin.to5CharAccount(providerName, 1);

        int seq = 1;
        String username = baseUsername;
        while (userMapper.selectByUsername(username) != null) {
            seq++;
            if (seq > 99) {
                throw BusinessException.fail("账号生成失败，服务商账号已超过99个上限");
            }
            username = ChineseToPinyin.to5CharAccount(providerName, seq);
        }

        return username;
    }

    public User getProviderAdminUser(String providerId) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getProviderId, providerId)
               .eq(User::getUserType, USER_TYPE_PROVIDER)
               .eq(User::getDeleted, 0);
        return userMapper.selectOne(wrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteProviderAccount(String providerId) {
        User user = getProviderAdminUser(providerId);
        if (user != null) {
            userRoleMapper.deleteByUserId(user.getUserId());
            userMapper.deleteById(user.getUserId());
            log.info("删除服务商管理员账号: userId={}, username={}", user.getUserId(), user.getUsername());
        }
    }

}
