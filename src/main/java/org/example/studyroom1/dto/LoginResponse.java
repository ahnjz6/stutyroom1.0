package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    
    private Long id;
    private String username;
    private String token;
    private Integer status;
    private Integer isVip;
}
