package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 修改用户状态响应DTO
 */
@Data
public class UpdateUserStatusResponse {
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 更新后的状态
     */
    private Integer status;
    
    public UpdateUserStatusResponse() {
    }
    
    public UpdateUserStatusResponse(Long userId, Integer status) {
        this.userId = userId;
        this.status = status;
    }
}
