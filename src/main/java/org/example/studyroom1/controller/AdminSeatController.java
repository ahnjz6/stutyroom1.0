package org.example.studyroom1.controller;

import lombok.RequiredArgsConstructor;
import org.example.studyroom1.common.Result;
import org.example.studyroom1.dto.UpdateSeatStatusRequest;
import org.example.studyroom1.service.StudyRoomService;
import org.springframework.web.bind.annotation.*;

/**
 * 管理员座位控制器
 */
@RestController
@RequestMapping("/admin/seat")
@RequiredArgsConstructor
public class AdminSeatController {
    
    private final StudyRoomService studyRoomService;
    
    /**
     * 修改座位信息
     */
    @PutMapping("")
    public Result<Object> updateSeatStatus(@RequestBody UpdateSeatStatusRequest request) {
        try {
            // 参数校验
            if (request.getSeatId() == null) {
                return Result.error("座位ID不能为空");
            }
            if (request.getHasPower() == null && request.getHasWindow() == null && request.getStatus() == null) {
                return Result.error("至少需要修改一个字段");
            }
            
            studyRoomService.updateSeatStatus(request);
            return Result.success("修改成功", new java.util.HashMap<>());
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("修改座位信息失败：" + e.getMessage());
        }
    }
}
