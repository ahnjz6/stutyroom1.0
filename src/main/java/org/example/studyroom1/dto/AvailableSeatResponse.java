package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 可用座位响应DTO
 */
@Data
public class AvailableSeatResponse {
    
    /**
     * 座位ID
     */
    private Long seatId;
    
    /**
     * 自习室ID
     */
    private Long roomId;
    
    /**
     * 自习室名称
     */
    private String roomName;
    
    /**
     * 是否有电源：0否 1是
     */
    private Integer hasPower;
    
    /**
     * 是否靠窗：0否 1是
     */
    private Integer hasWindow;
    
    /**
     * 状态：可用为1，不可用为0
     */
    private Integer status;
}
