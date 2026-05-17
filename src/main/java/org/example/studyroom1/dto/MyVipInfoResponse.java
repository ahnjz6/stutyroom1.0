package org.example.studyroom1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 我的VIP信息响应DTO
 */
@Data
public class MyVipInfoResponse {
    
    /**
     * 是否为VIP：0否 1是
     */
    private Integer isVip;
    
    /**
     * 卡名称
     */
    private String cardName;
    
    /**
     * 过期时间
     */
    private LocalDateTime expireTime;
    
    /**
     * 剩余次数（次卡）
     */
    private Integer remainingTimes;
    
    /**
     * VIP等级/类型
     */
    private Integer level;
}