package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 用户列表响应DTO
 */
@Data
public class UserListResponse {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 状态：0封禁 1正常
     */
    private Integer status;
    
    /**
     * 违约次数（7天内）
     */
    private Integer violationCount;
    
    /**
     * 是否VIP：0否 1是
     */
    private Integer isVip;
    
    /**
     * 创建时间
     */
    private String createTime;
}
