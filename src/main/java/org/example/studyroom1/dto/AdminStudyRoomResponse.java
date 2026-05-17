package org.example.studyroom1.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * 管理员自习室响应DTO
 */
@Data
public class AdminStudyRoomResponse {
    
    /**
     * 自习室ID
     */
    private Long id;
    
    /**
     * 自习室名称
     */
    private String name;
    
    /**
     * 位置
     */
    private String location;
    
    /**
     * 开放时间
     */
    private String openTime;
    
    /**
     * 关闭时间
     */
    private String closeTime;
    
    /**
     * 总座位数
     */
    private Integer totalSeats;
}
