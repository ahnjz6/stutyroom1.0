package org.example.studyroom1.dto;

import lombok.Data;

import java.util.List;

/**
 * 座位响应DTO
 */
@Data
public class SeatResponse {
    
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
     * 状态：0不可用 1可用
     */
    private Integer status;
    
    /**
     * 是否有电源：0否 1是
     */
    private Integer hasPower;
    
    /**
     * 是否靠窗：0否 1是
     */
    private Integer hasWindow;
    
    /**
     * 已预约时段列表
     */
    private List<String> bookedSlots;
}
