package org.example.studyroom1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 预约记录实体类
 */
@Data
@TableName("reservation")
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 预约ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 预约编号
     */
    private String reservationNo;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 座位ID
     */
    private Long seatId;

    /**
     * 自习室ID
     */
    private Long roomId;

    /**
     * 预约日期
     */
    private LocalDate reservationDate;

    /**
     * 开始时间（整点）
     */
    private LocalTime startTime;

    /**
     * 结束时间（整点）
     */
    private LocalTime endTime;

    /**
     * 预约时长（小时）
     */
    private Integer durationHours;

    /**
     * 状态：0待签到 1使用中 2已完成 3已取消 4已违约
     */
    private Integer status;

    /**
     * 签到时间
     */
    private LocalDateTime checkinTime;

    /**
     * 签退时间
     */
    private LocalDateTime checkoutTime;

    /**
     * 取消时间
     */
    private LocalDateTime cancelTime;

    /**
     * 是否提前结束：0否 1是
     */
    private Integer isEarlyCheckout;

    /**
     * 使用的VIP卡ID
     */
    private Long vipCardId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
