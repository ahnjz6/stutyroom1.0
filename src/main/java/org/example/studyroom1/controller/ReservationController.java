package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.AvailableSeatResponse;
import org.example.studyroom1.dto.QueryAvailableSeatsRequest;
import org.example.studyroom1.dto.BookSeatRequest;
import org.example.studyroom1.dto.BookSeatResponse;
import org.example.studyroom1.dto.MyReservationResponse;
import org.example.studyroom1.service.StudyRoomService;
import org.example.studyroom1.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 预约控制器
 */
@RestController
@RequestMapping("/user/reservation")
@RequiredArgsConstructor
public class ReservationController {
    
    private final StudyRoomService studyRoomService;
    
    /**
     * 按时段查询可用座位（用户端）
     * POST /user/reservation/queryAvailableSeats
     */
    @PostMapping("/queryAvailableSeats")
    public Result<List<AvailableSeatResponse>> queryAvailableSeats(
            @RequestHeader("Authorization") String authorization,
            @RequestBody QueryAvailableSeatsRequest request) {
        try {
            // 从 Token 中验证用户身份
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            List<AvailableSeatResponse> list = studyRoomService.queryAvailableSeats(request);
            return Result.success("查询成功", list);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询可用座位失败：" + e.getMessage());
        }
    }
    
    /**
     * 预约座位（用户端）
     * POST /user/reservation/bookBySeat
     */
    @PostMapping("/bookBySeat")
    public Result<BookSeatResponse> bookSeat(
            @RequestHeader("Authorization") String authorization,
            @RequestBody BookSeatRequest request) {
        try {
            // 从 Token 中验证用户身份
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            BookSeatResponse response = studyRoomService.bookSeat(userId, request);
            return Result.success("预约成功", response);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("预约失败：" + e.getMessage());
        }
    }
    
    /**
     * 分页查询我的预约列表（用户端）
     * GET /user/reservation/myReservations
     */
    @GetMapping("/myReservations")
    public Result<Page<MyReservationResponse>> getMyReservations(
            @RequestHeader("Authorization") String authorization,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "status", required = false) Integer status) {
        try {
            // 从 Token 中验证用户身份
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            // 参数校验
            if (page < 1) {
                page = 1;
            }
            if (pageSize < 1 || pageSize > 100) {
                pageSize = 10;
            }
            
            Page<MyReservationResponse> result = studyRoomService.getUserReservations(userId, page, pageSize, status);
            return Result.success("查询成功", result);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("查询预约列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 取消预约（用户端）
     * PUT /user/reservation/{id}/cancel
     */
    @PutMapping("/{id}/cancel")
    public Result<Object> cancelReservation(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("id") Long reservationId) {
        try {
            // 从 Token 中验证用户身份
            String token = authorization.replace("Bearer ", "");
            Long userId = JwtUtil.getUserIdFromToken(token);
            
            if (userId == null) {
                return Result.error("无效的Token");
            }
            
            // 参数校验
            if (reservationId == null) {
                return Result.error("预约ID不能为空");
            }
            
            studyRoomService.cancelReservation(userId, reservationId);
            return Result.success("取消成功", new java.util.HashMap<>());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("取消预约失败：" + e.getMessage());
        }
    }
}
