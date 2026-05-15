package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
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
     * 获取自习室列表
     */
    @GetMapping("/list")
    public Result<List<StudyRoomResponse>> getStudyRoomList() {
        try {
            List<StudyRoomResponse> list = studyRoomService.getStudyRoomList();
            return Result.success("获取成功", list);
        } catch (Exception e) {
            return Result.error("获取自习室列表失败：" + e.getMessage());
        }
    }
}
