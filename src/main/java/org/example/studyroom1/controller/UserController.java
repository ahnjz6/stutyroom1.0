package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.LoginRequest;
import org.example.studyroom1.dto.LoginResponse;
import org.example.studyroom1.dto.MessageListRequest;
import org.example.studyroom1.dto.MessageReadResponse;
import org.example.studyroom1.dto.MessageResponse;
import org.example.studyroom1.dto.MyVipInfoResponse;
import org.example.studyroom1.dto.PageResponse;
import org.example.studyroom1.dto.PurchaseVipRequest;
import org.example.studyroom1.dto.PurchaseVipResponse;
import org.example.studyroom1.dto.VipCardListResponse;
import org.example.studyroom1.service.UserService;
import org.example.studyroom1.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    
    /**
     * 获取用户消息列表（分页）
     */
    @GetMapping("/message/list")
    public Result<PageResponse<MessageResponse>> getMessageList(
            @RequestHeader("Authorization") String authorization,
            @RequestParam Integer page,
            @RequestParam Integer pageSize) {
        try {
            // 从 Token 中获取 userId
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            MessageListRequest request = new MessageListRequest();
            request.setPage(page);
            request.setPageSize(pageSize);
            
            PageResponse<MessageResponse> response = userService.getMessageList(userId, request);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 将消息标记为已读
     */
    @PutMapping("/message/{id}/read")
    public Result<MessageReadResponse> markMessageAsRead(
            @RequestHeader("Authorization") String authorization,
            @PathVariable Long id) {
        try {
            // 从 Token 中获取 userId
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            MessageReadResponse response = userService.markMessageAsRead(userId, id);
            return Result.success("标记成功", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 获取VIP卡列表
     */
    @GetMapping("/vip/cards")
    public Result<List<VipCardListResponse>> getVipCardList() {
        try {
            List<VipCardListResponse> list = userService.getVipCardList();
            return Result.success(list);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 查询我的VIP信息
     */
    @GetMapping("/vip/myVip")
    public Result<MyVipInfoResponse> getMyVipInfo(
            @RequestHeader("Authorization") String authorization) {
        try {
            // 从 Token 中获取 userId
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            MyVipInfoResponse response = userService.getMyVipInfo(userId);
            return Result.success(response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
    
    /**
     * 用户购买VIP
     */
    @PostMapping("/vip/purchase")
    public Result<PurchaseVipResponse> purchaseVip(
            @RequestHeader("Authorization") String authorization,
            @RequestBody PurchaseVipRequest request) {
        try {
            // 从 Token 中获取 userId
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            PurchaseVipResponse response = userService.purchaseVip(userId, request);
            return Result.success("购买成功", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }
}
