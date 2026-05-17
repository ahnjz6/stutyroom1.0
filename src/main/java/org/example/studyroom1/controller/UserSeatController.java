package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.SeatDetailResponse;
import org.example.studyroom1.service.StudyRoomService;
import org.example.studyroom1.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端座位控制器
 */
@RestController
@RequestMapping("/user/seat")
@RequiredArgsConstructor
public class UserSeatController {
    
    private final StudyRoomService studyRoomService;
    
    /**
     * 获取座位详情信息
     * GET /user/seat/message
     */
    @GetMapping("/message")
    public Result<SeatDetailResponse> getSeatMessage(
            @RequestHeader("Authorization") String authorization,
            @RequestParam String date,
            @RequestParam Integer seatId) {
        try {
            // 从 Token 中验证用户身份
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            SeatDetailResponse response = studyRoomService.getSeatDetail(seatId, date);
            return Result.success("获取成功", response);
        } catch (Exception e) {
            return Result.error("获取座位详情失败：" + e.getMessage());
        }
    }
}
