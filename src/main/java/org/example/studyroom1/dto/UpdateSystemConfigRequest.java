package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 修改系统配置请求DTO
 */
@Data
public class UpdateSystemConfigRequest {
    
    /**
     * 最大提前预约天数
     */
    private Integer maxAdvanceDays;
    
    /**
     * 单次预约最长时长（小时）
     */
    private Integer maxBookingHours;
    
    /**
     * 单次预约最短时长（小时）
     */
    private Integer minBookingHours;
    
    /**
     * 违约封禁次数
     */
    private Integer violationLimit;
    
    /**
     * 签到时间窗口（分钟）
     */
    private Integer checkInWindow;
    
    /**
     * 可提前签到分钟数
     */
    private Integer checkInBeforeMinutes;
}
