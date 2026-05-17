package org.example.studyroom1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 预约响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSeatResponse {
    
    /**
     * 预约ID
     */
    private Long reservationId;
    
    /**
     * 预约编号
     */
    private String reservationNo;
}
