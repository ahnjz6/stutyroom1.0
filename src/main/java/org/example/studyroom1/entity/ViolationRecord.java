package org.example.studyroom1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 违约记录实体类
 */
@Data
@TableName("violation_record")
public class ViolationRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 违约记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 预约ID
     */
    private Long reservationId;

    /**
     * 违约类型：1超时未签到
     */
    private Integer violationType;

    /**
     * 违约发生时间
     */
    private LocalDateTime violationTime;

    /**
     * 申诉状态：0未申诉 1申诉中 2申诉成功 3申诉失败
     */
    private Integer appealStatus;

    /**
     * 申诉理由
     */
    private String appealReason;

    /**
     * 申诉证明材料URL
     */
    private String appealProof;

    /**
     * 申诉时间
     */
    private LocalDateTime appealTime;

    /**
     * 申诉处理结果
     */
    private String appealResult;

    /**
     * 处理人
     */
    private String appealHandler;

    /**
     * 处理时间
     */
    private LocalDateTime appealHandleTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
