package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.LoginRequest;
import org.example.studyroom1.dto.LoginResponse;
import org.example.studyroom1.service.UserService;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;
    
    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = userService.login(request);
            return Result.success("登录成功", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    public Result<Object> logout(@RequestBody(required = false) Object body) {
        // JWT是无状态的，前端删除token即可
        // 后端可以记录退出日志或进行其他清理操作
        return Result.success("退出成功", new java.util.HashMap<>());
    }
}
