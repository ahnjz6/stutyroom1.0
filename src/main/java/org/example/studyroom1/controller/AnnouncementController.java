package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.AnnouncementResponse;
import org.example.studyroom1.service.AnnouncementService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告控制器
 */
@RestController
@RequestMapping("/user/announcement")
@RequiredArgsConstructor
public class AnnouncementController {
    
    private final AnnouncementService announcementService;
    
    /**
     * 获取公告列表
     */
    @GetMapping("/list")
    public Result<List<AnnouncementResponse>> getAnnouncementList() {
        try {
            List<AnnouncementResponse> list = announcementService.getAnnouncementList();
            return Result.success("获取成功", list);
        } catch (Exception e) {
            return Result.error("获取公告列表失败：" + e.getMessage());
        }
    }
}
