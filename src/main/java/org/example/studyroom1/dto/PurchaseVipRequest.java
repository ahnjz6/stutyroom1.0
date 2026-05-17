package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 购买VIP请求DTO
 */
@Data
public class PurchaseVipRequest {
    
    /**
     * VIP卡类型ID
     */
    private Integer cardTypeId;
}
