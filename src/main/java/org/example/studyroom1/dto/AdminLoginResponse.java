package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 管理员登录响应DTO
 */
@Data
public class AdminLoginResponse {
    
    /**
     * 管理员ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * JWT Token
     */
    private String token;
}
