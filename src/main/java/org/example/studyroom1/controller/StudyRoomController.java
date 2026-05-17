package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.UserSeatResponse;
import org.example.studyroom1.dto.UserStudyRoomResponse;
import org.example.studyroom1.dto.StudyRoomResponse;
import org.example.studyroom1.service.StudyRoomService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 自习室控制器
 */
@RestController
@RequestMapping("/user/room")
@RequiredArgsConstructor
public class StudyRoomController {
    
    private final StudyRoomService studyRoomService;
    
    /**
     * 获取自习室列表（用户端）
     */
    @GetMapping("/list")
    public Result<List<UserStudyRoomResponse>> getStudyRoomList() {
        try {
            List<UserStudyRoomResponse> list = studyRoomService.getUserStudyRoomList();
            return Result.success("获取成功", list);
        } catch (Exception e) {
            return Result.error("获取自习室列表失败：" + e.getMessage());
        }
    }
    
    /**
     * 获取自习室座位平面图（用户端）
     */
    @GetMapping("/{roomId}/seats")
    public Result<List<UserSeatResponse>> getSeatList(@PathVariable Long roomId) {
        try {
            List<UserSeatResponse> list = studyRoomService.getUserSeatList(roomId);
            return Result.success("获取成功", list);
        } catch (Exception e) {
            return Result.error("获取座位列表失败：" + e.getMessage());
        }
    }
}
