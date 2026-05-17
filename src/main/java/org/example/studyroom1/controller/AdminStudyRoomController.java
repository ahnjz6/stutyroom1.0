package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.AdminStudyRoomResponse;
import org.example.studyroom1.dto.CreateStudyRoomRequest;
import org.example.studyroom1.dto.SeatResponse;
import org.example.studyroom1.entity.StudyRoom;
import org.example.studyroom1.service.StudyRoomService;
import org.example.studyroom1.util.JwtUtil;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 管理员自习室控制器
 */
@RestController
@RequestMapping("/admin/room")
@RequiredArgsConstructor
public class AdminStudyRoomController {
    
    private final StudyRoomService studyRoomService;
    
    /**
     * 查看自习室列表
     */
    @GetMapping("/list")
    public Result<List<AdminStudyRoomResponse>> getStudyRoomList(
            @RequestHeader("Authorization") String authorization) {
        try {
            // 从 Token 中验证管理员身份
            String token = authorization.replace("Bearer ", "");
            Long adminId = JwtUtil.getUserIdFromToken(token);
            
            if (adminId == null) {
                return Result.error("无效的Token");
            }
            
            List<AdminStudyRoomResponse> list = studyRoomService.getAdminStudyRoomList();
            return Result.success("获取成功", list);
        } catch (Exception e) {
            return Result.error("获取自习室列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 查看座位列表
     */
    @GetMapping("/{roomId}/seat/list")
    public Result<List<SeatResponse>> getSeatList(
            @RequestHeader("Authorization") String authorization,
            @PathVariable("roomId") Long roomId) {
        try {
            // 从 Token 中验证管理员身份
            String token = authorization.replace("Bearer ", "");
            Long adminId = JwtUtil.getUserIdFromToken(token);
            
            if (adminId == null) {
                return Result.error("无效的Token");
            }
            
            // 参数校验
            if (roomId == null) {
                return Result.error("自习室ID不能为空");
            }
            
            List<SeatResponse> list = studyRoomService.getSeatListByRoomId(roomId);
            return Result.success("获取成功", list);
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("获取座位列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 新增自习室
     */
    @PostMapping("")
    public Result<StudyRoom> createStudyRoom(
            @RequestHeader("Authorization") String authorization,
            @RequestBody CreateStudyRoomRequest request) {
        try {
            // 从 Token 中验证管理员身份
            String token = authorization.replace("Bearer ", "");
            Long adminId = JwtUtil.getUserIdFromToken(token);
            
            if (adminId == null) {
                return Result.error("无效的Token");
            }
            
            // 参数校验
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return Result.error("自习室名称不能为空");
            }
            if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
                return Result.error("位置不能为空");
            }
            if (request.getOpenTime() == null || request.getOpenTime().trim().isEmpty()) {
                return Result.error("开放时间不能为空");
            }
            if (request.getCloseTime() == null || request.getCloseTime().trim().isEmpty()) {
                return Result.error("关闭时间不能为空");
            }
            if (request.getTotalRow() == null || request.getTotalRow() <= 0) {
                return Result.error("总行数必须大于0");
            }
            if (request.getTotalCol() == null || request.getTotalCol() <= 0) {
                return Result.error("总列数必须大于0");
            }
            
            StudyRoom studyRoom = studyRoomService.createStudyRoom(request);
            return Result.success("新增成功", studyRoom);
        } catch (Exception e) {
            return Result.error("新增自习室失败：" + e.getMessage());
        }
    }
    
    /**
     * 启动/禁用自习室
     */
    @PostMapping("/status/{status}")
    public Result<Object> updateStudyRoomStatus(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("id") Long id, 
            @PathVariable("status") Integer status) {
        try {
            // 从 Token 中验证管理员身份
            String token = authorization.replace("Bearer ", "");
            Long adminId = JwtUtil.getUserIdFromToken(token);
            
            if (adminId == null) {
                return Result.error("无效的Token");
            }
            
            // 参数校验
            if (id == null) {
                return Result.error("自习室ID不能为空");
            }
            if (status == null || (status != 0 && status != 1)) {
                return Result.error("状态值必须为0或1");
            }
            
            studyRoomService.updateStudyRoomStatus(id, status);
            
            String statusMsg = status == 1 ? "启动" : "禁用";
            return Result.success(statusMsg + "成功", new java.util.HashMap<>());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新状态失败：" + e.getMessage());
        }
    }
}
