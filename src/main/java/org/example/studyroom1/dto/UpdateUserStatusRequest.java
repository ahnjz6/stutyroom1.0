package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 修改用户状态请求DTO
 */
@Data
public class UpdateUserStatusRequest {
    
    /**
     * 状态：0正常 1封禁
     */
    private Integer status;
}
