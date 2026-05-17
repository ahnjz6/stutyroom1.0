package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 预约座位请求DTO
 */
@Data
public class BookSeatRequest {
    
    /**
     * 座位ID
     */
    private Long seatId;
    
    /**
     * 日期（格式：yyyy-MM-dd）
     */
    private String date;
    
    /**
     * 开始时间（格式：HH:mm）
     */
    private String startTime;
    
    /**
     * 结束时间（格式：HH:mm）
     */
    private String endTime;
}
