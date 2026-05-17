package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 修改座位状态请求DTO
 */
@Data
public class UpdateSeatStatusRequest {
    
    /**
     * 座位ID
     */
    private Long seatId;
    
    /**
     * 是否有电源：0否 1是
     */
    private Integer hasPower;
    
    /**
     * 是否靠窗：0否 1是
     */
    private Integer hasWindow;
    
    /**
     * 状态：0不可用 1可用
     */
    private Integer status;
}
