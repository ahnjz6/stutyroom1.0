package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 签到请求DTO
 */
@Data
public class CheckinRequest {
    
    /**
     * 预约ID
     */
    private Long reservationId;
}
