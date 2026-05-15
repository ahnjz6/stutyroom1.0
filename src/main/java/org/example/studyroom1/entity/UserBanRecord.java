package org.example.studyroom1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户封禁记录实体类
 */
@Data
@TableName("user_ban_record")
public class UserBanRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 封禁记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 封禁原因
     */
    private String banReason;

    /**
     * 封禁开始时间
     */
    private LocalDateTime banStartTime;

    /**
     * 封禁结束时间
     */
    private LocalDateTime banEndTime;

    /**
     * 触发封禁时的违约次数
     */
    private Integer violationCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}
