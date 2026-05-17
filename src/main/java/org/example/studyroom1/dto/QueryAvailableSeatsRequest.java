package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 按时段查询可用座位请求DTO
 */
@Data
public class QueryAvailableSeatsRequest {
    
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
    
    /**
     * 自习室ID（可选，不传则查询所有自习室）
     */
    private Long roomId;
}
