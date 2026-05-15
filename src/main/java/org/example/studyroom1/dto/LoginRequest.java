package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    
    private String username;
    private String password;
}
