package org.example.studyroom1.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 公告响应DTO
 */
@Data
public class AnnouncementResponse {
    
    private Long id;
    private String title;
    private String content;
    private Integer isTop;
    private LocalDateTime createTime;
}
