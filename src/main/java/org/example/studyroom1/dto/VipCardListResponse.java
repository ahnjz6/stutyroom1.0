package org.example.studyroom1.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * VIP卡列表响应DTO
 */
@Data
public class VipCardListResponse {
    
    /**
     * 卡类型ID
     */
    private Integer id;
    
    /**
     * 卡种名称
     */
    private String name;
    
    /**
     * 类型：1次卡 2月卡 3年卡
     */
    private Integer type;
    
    /**
     * 售价
     */
    private BigDecimal price;
    
    /**
     * 有效期天数（月卡/年卡）或可用次数（次卡）
     */
    private Integer duration;
    
    /**
     * 单次预约最长时长（小时）
     */
    private Integer maxBookingHours;
    
    /**
     * 可提前预约天数
     */
    private Integer advanceDays;
    
    /**
     * 违约次数阈值
     */
    private Integer violationLimit;
}
