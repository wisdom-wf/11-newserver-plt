package com.elderlycare.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.elderlycare.common.BusinessException;
import com.elderlycare.common.IDGenerator;
import com.elderlycare.dto.UserDTO;
import com.elderlycare.entity.User;
import com.elderlycare.entity.UserRole;
import com.elderlycare.mapper.UserMapper;
import com.elderlycare.mapper.UserRoleMapper;
import com.elderlycare.service.UserService;
import com.elderlycare.vo.RoleVO;
import com.elderlycare.vo.UserVO;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.crypto.symmetric.SymmetricAlgorithm;
import cn.hutool.crypto.symmetric.SymmetricCrypto;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public Page<UserVO> pageUsers(int current, int size, String username, String status, String userType) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();

        if (StringUtils.hasText(username)) {
            queryWrapper.like(User::getUsername, username);
        }
        if (StringUtils.hasText(status)) {
            queryWrapper.eq(User::getStatus, status);
        }
        if (StringUtils.hasText(userType)) {
            queryWrapper.eq(User::getUserType, userType);
        }

        Page<User> userPage = page(page, queryWrapper);

        // 转换为VO
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        List<UserVO> voList = userPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);

        return voPage;
    }

    @Override
    public UserVO getUserById(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        UserVO userVO = convertToVO(user);
        // 填充角色信息
        List<String> roleIds = userMapper.selectRoleIdsByUserId(userId);
        if (!roleIds.isEmpty()) {
            List<RoleVO> roles = userRoleMapper.selectRoleIdsByUserId(userId) != null ?
                    getRolesByUserId(userId) : new ArrayList<>();
            userVO.setRoles(roles);
        }
        return userVO;
    }

    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean addUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        User existUser = userMapper.selectByUsername(userDTO.getUsername());
        if (existUser != null) {
            throw new BusinessException(400, "用户名已存在");
        }

        // 加密密码
        String encodedPassword = encodePassword(userDTO.getPassword());

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);
        user.setUserId(IDGenerator.generateId());
        user.setPassword(encodedPassword);
        user.setStatus("NORMAL");

        int result = userMapper.insert(user);

        // 分配角色
        if (result > 0 && userDTO.getRoleIds() != null && !userDTO.getRoleIds().isEmpty()) {
            assignRoles(user.getUserId(), userDTO.getRoleIds());
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateUser(UserDTO userDTO) {
        User existUser = userMapper.selectById(userDTO.getUserId());
        if (existUser == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 如果修改了用户名，检查是否重复
        if (!existUser.getUsername().equals(userDTO.getUsername())) {
            User duplicateUser = userMapper.selectByUsername(userDTO.getUsername());
            if (duplicateUser != null) {
                throw new BusinessException(400, "用户名已存在");
            }
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        int result = userMapper.updateById(user);

        // 更新角色
        if (result > 0 && userDTO.getRoleIds() != null) {
            assignRoles(userDTO.getUserId(), userDTO.getRoleIds());
        }

        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        // 逻辑删除
        return userMapper.deleteById(userId) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(String userId, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }

        String encodedPassword = encodePassword(newPassword);

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getUserId, userId)
                .set(User::getPassword, encodedPassword);

        return userMapper.update(null, updateWrapper) > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoles(String userId, List<String> roleIds) {
        // 删除现有角色关联
        userRoleMapper.deleteByUserId(userId);

        // 添加新角色关联
        if (roleIds != null && !roleIds.isEmpty()) {
            List<UserRole> userRoles = roleIds.stream().map(roleId -> {
                UserRole ur = new UserRole();
                ur.setUserRoleId(IDGenerator.generateId());
                ur.setUserId(userId);
                ur.setRoleId(roleId);
                return ur;
            }).collect(Collectors.toList());

            userRoleMapper.batchInsert(userRoles);
        }

        return true;
    }

    @Override
    public List<String> getUserRoleIds(String userId) {
        return userMapper.selectRoleIdsByUserId(userId);
    }

    @Override
    public List<String> getUserRoleCodes(String userId) {
        return userMapper.selectRoleCodesByUserId(userId);
    }

    @Override
    public List<String> getUserPermissionCodes(String userId) {
        return userMapper.selectPermissionCodesByUserId(userId);
    }

    /**
     * 转换为VO
     */
    private UserVO convertToVO(User user) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        // 手动映射username -> userName (字段名不一致)
        userVO.setUserName(user.getUsername());
        return userVO;
    }

    /**
     * 根据用户ID获取角色列表
     */
    private List<RoleVO> getRolesByUserId(String userId) {
        return new ArrayList<>();
    }

    /**
     * 密码加密（使用AES）
     */
    private static final String AES_KEY = "ElderlyCarePwdKey2026";

    /**
     * 密码加密（使用AES）
     */
    private String encodePassword(String password) {
        byte[] key = AES_KEY.getBytes();
        SymmetricCrypto aes = new SymmetricCrypto(SymmetricAlgorithm.AES, key);
        byte[] encrypted = aes.encrypt(password);
        return Base64.getEncoder().encodeToString(encrypted);
    }
}
