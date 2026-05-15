package org.example.studyroom1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.studyroom1.dto.LoginRequest;
import org.example.studyroom1.dto.LoginResponse;
import org.example.studyroom1.entity.User;
import org.example.studyroom1.entity.UserLoginLog;
import org.example.studyroom1.entity.UserVipCard;
import org.example.studyroom1.mapper.UserLoginLogMapper;
import org.example.studyroom1.mapper.UserMapper;
import org.example.studyroom1.mapper.UserVipCardMapper;
import org.example.studyroom1.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final UserLoginLogMapper userLoginLogMapper;
    private final UserVipCardMapper userVipCardMapper;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        
        // 查询用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        
        if (user == null) {
            // 记录登录失败日志
            recordLoginLog(null, username, 0, "用户不存在");
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查账号状态
        if (user.getStatus() == 0) {
            recordLoginLog(user.getId(), username, 0, "账号已被封禁");
            throw new RuntimeException("账号已被封禁，请联系管理员");
        }
        
        // 检查账号是否锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            recordLoginLog(user.getId(), username, 0, "账号已锁定");
            throw new RuntimeException("账号已锁定，请稍后再试");
        }
        
        // 验证密码（明文比对）
        if (!password.equals(user.getPassword())) {
            // 更新登录失败次数
            updateLoginFailCount(user);
            recordLoginLog(user.getId(), username, 0, "密码错误");
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 登录成功，重置失败次数
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);
        
        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 检查是否为VIP
        Integer isVip = checkVipStatus(user.getId());
        
        // 记录登录成功日志
        recordLoginLog(user.getId(), username, 1, null);
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setToken(token);
        response.setStatus(user.getStatus());
        response.setIsVip(isVip);
        
        return response;
    }
    
    /**
     * 更新登录失败次数
     */
    private void updateLoginFailCount(User user) {
        user.setLoginFailCount(user.getLoginFailCount() + 1);
        
        // 如果连续失败5次，锁定账号30分钟
        if (user.getLoginFailCount() >= 5) {
            user.setLockTime(LocalDateTime.now().plusMinutes(30));
        }
        
        userMapper.updateById(user);
    }
    
    /**
     * 检查VIP状态
     */
    private Integer checkVipStatus(Long userId) {
        // 查询用户是否有生效的VIP卡
        Long count = userVipCardMapper.selectCount(
            new LambdaQueryWrapper<UserVipCard>()
                .eq(UserVipCard::getUserId, userId)
                .eq(UserVipCard::getStatus, 1) // 生效中
                .le(UserVipCard::getStartTime, LocalDateTime.now())
                .ge(UserVipCard::getEndTime, LocalDateTime.now())
        );
        
        return count > 0 ? 1 : 0;
    }
    
    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String username, Integer status, String failReason) {
        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setLoginTime(LocalDateTime.now());
        log.setStatus(status);
        log.setFailReason(failReason);
        // TODO: 从请求中获取IP和User-Agent
        log.setLoginIp("127.0.0.1");
        log.setUserAgent("Unknown");
        
        userLoginLogMapper.insert(log);
    }
}
