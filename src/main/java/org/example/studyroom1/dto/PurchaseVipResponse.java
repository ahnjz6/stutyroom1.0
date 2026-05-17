package org.example.studyroom1.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 购买VIP响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseVipResponse {
    
    /**
     * 订单号（购买记录ID）
     */
    private Long orderId;
}
