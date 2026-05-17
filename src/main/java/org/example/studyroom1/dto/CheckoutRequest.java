package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 签退请求DTO
 */
@Data
public class CheckoutRequest {
    
    /**
     * 预约ID
     */
    private Long reservationId;
}
