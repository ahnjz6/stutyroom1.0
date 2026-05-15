package org.example.studyroom1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 座位时段占用实体类
 */
@Data
@TableName("seat_occupancy")
public class SeatOccupancy implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 座位ID
     */
    private Long seatId;

    /**
     * 日期
     */
    private LocalDate occupancyDate;

    /**
     * 小时时段（整点：0-23）
     */
    private Integer hourSlot;

    /**
     * 预约ID
     */
    private Long reservationId;

    /**
     * 状态：0空闲 1已预约 2使用中
     */
    private Integer status;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
