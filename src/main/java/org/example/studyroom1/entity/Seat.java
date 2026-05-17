package org.example.studyroom1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableField;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 座位实体类
 */
@Data
@TableName("seat")
public class Seat implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 座位ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属自习室ID
     */
    private Long roomId;

    /**
     * 座位编号
     */
    private String seatNumber;

    /**
     * 行号
     */
    @TableField("`row_number`")
    private Integer rowNumber;

    /**
     * 列号
     */
    @TableField("`col_number`")
    private Integer colNumber;

    /**
     * 是否有电源：0无 1有
     */
    private Integer hasPower;

    /**
     * 是否靠窗：0否 1是
     */
    private Integer isWindow;

    /**
     * 状态：0维修/禁用 1可用
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
