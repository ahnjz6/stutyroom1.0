package org.example.studyroom1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 自习室实体类
 */
@Data
@TableName("study_room")
public class StudyRoom implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 自习室ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 自习室名称
     */
    private String name;

    /**
     * 所属楼栋
     */
    private String building;

    /**
     * 所在楼层
     */
    private String floor;

    /**
     * 位置描述
     */
    private String locationDesc;

    /**
     * 开放开始时间
     */
    private LocalTime openTime;

    /**
     * 开放结束时间
     */
    private LocalTime closeTime;

    /**
     * 总座位数
     */
    private Integer totalSeats;

    /**
     * 状态：0停用 1启用
     */
    private Integer status;

    /**
     * 安静程度：1-5
     */
    private Integer quietLevel;

    /**
     * 排序优先级
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
