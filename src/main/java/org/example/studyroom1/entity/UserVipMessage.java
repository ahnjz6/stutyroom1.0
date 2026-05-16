package org.example.studyroom1.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user_vip_message")
public class UserVipMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 用户ID
     */
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;

    /**
     * 是否是VIP会员
     */
    private boolean isVip;

    /**
     * 用户名
     */
    private String name;

    /**
     * VIP会员到期时间
     */
    private LocalDate endTime;

    /**
     * 剩余使用次数
     */
    private Integer remainingCount;

    /**
     * 会员等级
     */
    private String Level;
}