package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 用户端座位响应DTO（仅包含座位平面图所需字段）
 */
@Data
public class UserSeatResponse {
    
    /**
     * 座位ID
     */
    private Long seatId;
    
    /**
     * 行号
     */
    private Integer row;
    
    /**
     * 列号
     */
    private Integer col;
    
    /**
     * 状态：1可用，0不可用
     */
    private Integer status;
}
