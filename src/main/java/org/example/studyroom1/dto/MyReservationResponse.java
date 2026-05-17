package org.example.studyroom1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 预约记录响应DTO
 */
@Data
public class MyReservationResponse {
    
    /**
     * 预约ID
     */
    private Long id;
    
    /**
     * 预约编号
     */
    private String reservationNo;
    
    /**
     * 自习室名称
     */
    private String roomName;
    
    /**
     * 座位号
     */
    private String seatNo;
    
    /**
     * 日期
     */
    private String date;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * 状态：0待签到 1使用中 2已完成 3已取消 4已违约
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
