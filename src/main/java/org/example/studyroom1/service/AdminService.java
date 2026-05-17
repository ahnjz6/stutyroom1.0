package org.example.studyroom1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.studyroom1.dto.AdminLoginRequest;
import org.example.studyroom1.dto.AdminLoginResponse;
import org.example.studyroom1.dto.AnnouncementListResponse;
import org.example.studyroom1.dto.CreateAnnouncementRequest;
import org.example.studyroom1.dto.CreateAnnouncementResponse;
import org.example.studyroom1.dto.PageResponse;
import org.example.studyroom1.dto.SystemConfigResponse;
import org.example.studyroom1.dto.UpdateSystemConfigRequest;
import org.example.studyroom1.dto.UpdateSystemConfigResponse;
import org.example.studyroom1.dto.UpdateUserStatusRequest;
import org.example.studyroom1.dto.UpdateUserStatusResponse;
import org.example.studyroom1.dto.UserListResponse;
import org.example.studyroom1.entity.Admin;
import org.example.studyroom1.entity.AdminLoginLog;
import org.example.studyroom1.entity.Announcement;
import org.example.studyroom1.entity.SystemConfig;
import org.example.studyroom1.entity.User;
import org.example.studyroom1.entity.UserVipCard;
import org.example.studyroom1.mapper.AdminLoginLogMapper;
import org.example.studyroom1.mapper.AdminMapper;
import org.example.studyroom1.mapper.AnnouncementMapper;
import org.example.studyroom1.mapper.SystemConfigMapper;
import org.example.studyroom1.mapper.UserMapper;
import org.example.studyroom1.mapper.UserVipCardMapper;
import org.example.studyroom1.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员服务类
 */
@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final AdminMapper adminMapper;
    private final AdminLoginLogMapper adminLoginLogMapper;
    private final UserMapper userMapper;
    private final UserVipCardMapper userVipCardMapper;
    private final AnnouncementMapper announcementMapper;
    private final SystemConfigMapper systemConfigMapper;
    
    /**
     * 管理员登录
     */
    public AdminLoginResponse login(AdminLoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        
        // 查询管理员
        Admin admin = adminMapper.selectOne(
            new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username)
        );
        
        if (admin == null) {
            // 记录登录失败日志
            recordLoginLog(null, username, 0, "管理员不存在");
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查账号状态
        if (admin.getStatus() == 0) {
            recordLoginLog(admin.getId(), username, 0, "账号已被禁用");
            throw new RuntimeException("账号已被禁用，请联系超级管理员");
        }
        
        // 验证密码（明文比对）
        if (!password.equals(admin.getPassword())) {
            recordLoginLog(admin.getId(), username, 0, "密码错误");
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 更新最后登录信息
        admin.setLastLoginTime(LocalDateTime.now());
        admin.setLastLoginIp("127.0.0.1"); // TODO: 从请求中获取真实IP
        adminMapper.updateById(admin);
        
        // 生成JWT Token
        String token = JwtUtil.generateToken(admin.getId(), admin.getUsername());
        
        // 记录登录成功日志
        recordLoginLog(admin.getId(), username, 1, null);
        
        // 构建响应
        AdminLoginResponse response = new AdminLoginResponse();
        response.setId(admin.getId());
        response.setUsername(admin.getUsername());
        response.setToken(token);
        
        return response;
    }
    
    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long adminId, String username, Integer status, String failReason) {
        AdminLoginLog log = new AdminLoginLog();
        log.setAdminId(adminId);
        log.setLoginTime(LocalDateTime.now());
        log.setStatus(status);
        // TODO: 从请求中获取真实IP
        log.setLoginIp("127.0.0.1");
        
        adminLoginLogMapper.insert(log);
    }
    
    /**
     * 获取用户列表（分页）
     */
    public PageResponse<UserListResponse> getUserList(Integer page, Integer pageSize, String keyword) {
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 创建分页对象
        Page<User> userPage = new Page<>(page, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 如果有搜索关键词，按用户名模糊搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.like(User::getUsername, keyword.trim());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(User::getCreateTime);
        
        // 执行分页查询
        Page<User> resultPage = userMapper.selectPage(userPage, queryWrapper);
        
        // 转换为响应DTO
        List<UserListResponse> userResponses = resultPage.getRecords().stream()
            .map(this::convertToUserListResponse)
            .collect(Collectors.toList());
        
        // 构建分页响应
        return new PageResponse<>(
            resultPage.getTotal(),
            (int) resultPage.getCurrent(),
            (int) resultPage.getSize(),
            userResponses
        );
    }
    
    /**
     * 转换User实体为UserListResponse DTO
     */
    private UserListResponse convertToUserListResponse(User user) {
        UserListResponse response = new UserListResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setStatus(user.getStatus());
        response.setViolationCount(user.getViolationCount() != null ? user.getViolationCount() : 0);
        
        // 检查是否为VIP
        response.setIsVip(checkVipStatus(user.getId()));
        
        // 格式化创建时间
        if (user.getCreateTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            response.setCreateTime(user.getCreateTime().format(formatter));
        }
        
        return response;
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
     * 修改用户状态
     */
    public UpdateUserStatusResponse updateUserStatus(Long userId, UpdateUserStatusRequest request) {
        // 查询用户
        User user = userMapper.selectById(userId);
        
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        // 验证状态参数
        if (request.getStatus() == null || (request.getStatus() != 0 && request.getStatus() != 1)) {
            throw new RuntimeException("状态参数错误，只能为0或1");
        }
        
        // 更新用户状态
        user.setStatus(request.getStatus());
        userMapper.updateById(user);
        
        return new UpdateUserStatusResponse(userId, request.getStatus());
    }
    
    /**
     * 获取公告列表（分页）
     */
    public PageResponse<AnnouncementListResponse> getAnnouncementList(Integer page, Integer pageSize) {
        // 参数校验
        if (page == null || page < 1) {
            page = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        
        // 创建分页对象
        Page<Announcement> announcementPage = new Page<>(page, pageSize);
        
        // 构建查询条件，按置顶和创建时间排序
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Announcement::getIsPinned)
                   .orderByDesc(Announcement::getCreateTime);
        
        // 执行分页查询
        Page<Announcement> resultPage = announcementMapper.selectPage(announcementPage, queryWrapper);
        
        // 转换为响应DTO
        List<AnnouncementListResponse> announcementResponses = resultPage.getRecords().stream()
            .map(this::convertToAnnouncementListResponse)
            .collect(Collectors.toList());
        
        // 构建分页响应
        return new PageResponse<>(
            resultPage.getTotal(),
            (int) resultPage.getCurrent(),
            (int) resultPage.getSize(),
            announcementResponses
        );
    }
    
    /**
     * 转换Announcement实体为AnnouncementListResponse DTO
     */
    private AnnouncementListResponse convertToAnnouncementListResponse(Announcement announcement) {
        AnnouncementListResponse response = new AnnouncementListResponse();
        response.setId(announcement.getId());
        response.setTitle(announcement.getTitle());
        response.setContent(announcement.getContent());
        response.setIsTop(announcement.getIsPinned());
        response.setStatus(announcement.getStatus());
        
        // 格式化过期时间
        if (announcement.getExpireTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            response.setExpireTime(announcement.getExpireTime().format(formatter));
        }
        
        // 格式化创建时间
        if (announcement.getCreateTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            response.setCreateTime(announcement.getCreateTime().format(formatter));
        }
        
        return response;
    }
    
    /**
     * 新增公告
     */
    public CreateAnnouncementResponse createAnnouncement(CreateAnnouncementRequest request) {
        // 参数校验
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            throw new RuntimeException("公告标题不能为空");
        }
        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("公告内容不能为空");
        }
        if (request.getExpireTime() == null || request.getExpireTime().trim().isEmpty()) {
            throw new RuntimeException("过期时间不能为空");
        }
        
        // 创建公告实体
        Announcement announcement = new Announcement();
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setIsPinned(request.getIsTop() != null ? request.getIsTop() : 0);
        announcement.setStatus(1); // 默认已发布
        announcement.setPublishTime(LocalDateTime.now());
        
        // 解析过期时间
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            announcement.setExpireTime(LocalDateTime.parse(request.getExpireTime(), formatter));
        } catch (Exception e) {
            throw new RuntimeException("过期时间格式错误，请使用 yyyy-MM-dd HH:mm:ss 格式");
        }
        
        announcement.setCreateBy("admin"); // TODO: 从登录信息中获取
        announcement.setCreateTime(LocalDateTime.now());
        
        // 保存到数据库
        announcementMapper.insert(announcement);
        
        return new CreateAnnouncementResponse(announcement.getId(), announcement.getTitle(), announcement.getStatus());
    }
    
    /**
     * 获取系统配置
     */
    public SystemConfigResponse getSystemConfig() {
        // 查询所有未删除的配置
        List<SystemConfig> configs = systemConfigMapper.selectList(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getIsDeleted, 0)
        );
        
        // 构建响应对象
        SystemConfigResponse response = new SystemConfigResponse();
        
        // 将配置列表转换为Map，方便查找
        for (SystemConfig config : configs) {
            String key = config.getConfigKey();
            String value = config.getConfigValue();
            
            if (value != null) {
                try {
                    switch (key) {
                        case "maxAdvanceDays":
                            response.setMaxAdvanceDays(Integer.parseInt(value));
                            break;
                        case "maxBookingHours":
                            response.setMaxBookingHours(Integer.parseInt(value));
                            break;
                        case "minBookingHours":
                            response.setMinBookingHours(Integer.parseInt(value));
                            break;
                        case "violationLimit":
                            response.setViolationLimit(Integer.parseInt(value));
                            break;
                        case "checkInWindow":
                            response.setCheckInWindow(Integer.parseInt(value));
                            break;
                        case "checkInBeforeMinutes":
                            response.setCheckInBeforeMinutes(Integer.parseInt(value));
                            break;
                    }
                } catch (NumberFormatException e) {
                    // 如果转换失败，使用默认值
                }
            }
        }
        
        return response;
    }
    
    /**
     * 修改系统配置
     */
    public UpdateSystemConfigResponse updateSystemConfig(UpdateSystemConfigRequest request) {
        int updateCount = 0;
        LocalDateTime now = LocalDateTime.now();
        
        // 更新最大提前预约天数
        if (request.getMaxAdvanceDays() != null) {
            updateConfigValue("maxAdvanceDays", String.valueOf(request.getMaxAdvanceDays()));
            updateCount++;
        }
        
        // 更新单次预约最长时长
        if (request.getMaxBookingHours() != null) {
            updateConfigValue("maxBookingHours", String.valueOf(request.getMaxBookingHours()));
            updateCount++;
        }
        
        // 更新单次预约最短时长
        if (request.getMinBookingHours() != null) {
            updateConfigValue("minBookingHours", String.valueOf(request.getMinBookingHours()));
            updateCount++;
        }
        
        // 更新违约封禁次数
        if (request.getViolationLimit() != null) {
            updateConfigValue("violationLimit", String.valueOf(request.getViolationLimit()));
            updateCount++;
        }
        
        // 更新签到时间窗口
        if (request.getCheckInWindow() != null) {
            updateConfigValue("checkInWindow", String.valueOf(request.getCheckInWindow()));
            updateCount++;
        }
        
        // 更新可提前签到分钟数
        if (request.getCheckInBeforeMinutes() != null) {
            updateConfigValue("checkInBeforeMinutes", String.valueOf(request.getCheckInBeforeMinutes()));
            updateCount++;
        }
        
        // 格式化更新时间
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String updateTimeStr = now.format(formatter);
        
        return new UpdateSystemConfigResponse(updateCount, updateTimeStr);
    }
    
    /**
     * 更新单个配置项
     */
    private void updateConfigValue(String configKey, String configValue) {
        // 查询配置项
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, configKey)
                .eq(SystemConfig::getIsDeleted, 0)
        );
        
        if (config == null) {
            // 如果配置项不存在，创建新的
            config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            config.setIsDeleted(0);
            config.setCreateTime(LocalDateTime.now());
            config.setUpdateTime(LocalDateTime.now());
            systemConfigMapper.insert(config);
        } else {
            // 更新现有配置
            config.setConfigValue(configValue);
            config.setUpdateTime(LocalDateTime.now());
            systemConfigMapper.updateById(config);
        }
    }
}
