package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 用户端自习室响应DTO
 */
@Data
public class UserStudyRoomResponse {
    
    /**
     * 自习室ID
     */
    private Long id;
    
    /**
     * 自习室名称
     */
    private String name;
}
