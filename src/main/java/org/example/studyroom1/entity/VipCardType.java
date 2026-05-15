package org.example.studyroom1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VIP卡类型实体类
 */
@Data
@TableName("vip_card_type")
public class VipCardType implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 卡类型ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 卡种名称（如：次卡、月卡、年卡）
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
     * 有效期天数（月卡/年卡）
     */
    private Integer durationDays;

    /**
     * 可用次数（次卡）
     */
    private Integer usageCount;

    /**
     * 单次预约最长时长（小时）
     */
    private Integer maxReservationHours;

    /**
     * 可提前预约天数
     */
    private Integer advanceDays;

    /**
     * 违约次数阈值
     */
    private Integer violationThreshold;

    /**
     * 卡面图标URL
     */
    private String icon;

    /**
     * 排序优先级
     */
    private Integer sortOrder;

    /**
     * 状态：0下架 1上架
     */
    private Integer status;

    /**
     * 卡种说明
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
