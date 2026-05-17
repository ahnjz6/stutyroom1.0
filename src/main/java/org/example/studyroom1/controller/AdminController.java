package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.AdminLoginRequest;
import org.example.studyroom1.dto.AdminLoginResponse;
import org.example.studyroom1.dto.AnnouncementListResponse;
import org.example.studyroom1.dto.CreateAnnouncementRequest;
import org.example.studyroom1.dto.CreateAnnouncementResponse;
import org.example.studyroom1.dto.PageResponse;
import org.example.studyroom1.dto.UpdateUserStatusRequest;
import org.example.studyroom1.dto.UpdateUserStatusResponse;
import org.example.studyroom1.dto.UserListResponse;
import org.example.studyroom1.dto.SystemConfigResponse;
import org.example.studyroom1.dto.UpdateSystemConfigRequest;
import org.example.studyroom1.dto.UpdateSystemConfigResponse;
import org.example.studyroom1.service.AdminService;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员控制器
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    
    /**
     * 管理员登录
     */
    @PostMapping("/login")
    public Result<AdminLoginResponse> login(@RequestBody AdminLoginRequest request) {
        try {
            AdminLoginResponse response = adminService.login(request);
            return Result.success("登录成功", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取用户列表（分页）
     */
    @GetMapping("/user/list")
    public Result<PageResponse<UserListResponse>> getUserList(
            @RequestParam Integer page,
            @RequestParam Integer pageSize,
            @RequestParam(required = false) String keyword) {
        try {
            PageResponse<UserListResponse> response = adminService.getUserList(page, pageSize, keyword);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改用户状态
     */
    @PutMapping("/user/{id}/status")
    public Result<UpdateUserStatusResponse> updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateUserStatusRequest request) {
        try {
            UpdateUserStatusResponse response = adminService.updateUserStatus(id, request);
            return Result.success("修改成功", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取公告列表（分页）
     */
    @GetMapping("/announcement/list")
    public Result<PageResponse<AnnouncementListResponse>> getAnnouncementList(
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        try {
            PageResponse<AnnouncementListResponse> response = adminService.getAnnouncementList(page, pageSize);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 新增公告
     */
    @PostMapping("/announcement")
    public Result<CreateAnnouncementResponse> createAnnouncement(@RequestBody CreateAnnouncementRequest request) {
        try {
            CreateAnnouncementResponse response = adminService.createAnnouncement(request);
            return Result.success("发布成功", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取系统配置
     */
    @GetMapping("/system/config")
    public Result<SystemConfigResponse> getSystemConfig() {
        try {
            SystemConfigResponse response = adminService.getSystemConfig();
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 修改系统配置
     */
    @PutMapping("/system/config")
    public Result<UpdateSystemConfigResponse> updateSystemConfig(@RequestBody UpdateSystemConfigRequest request) {
        try {
            UpdateSystemConfigResponse response = adminService.updateSystemConfig(request);
            return Result.success("修改成功", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
