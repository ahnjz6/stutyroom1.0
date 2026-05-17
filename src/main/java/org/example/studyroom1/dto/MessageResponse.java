package org.example.studyroom1.dto;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 消息响应DTO
 */
@Data
public class MessageResponse {
    
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 消息标题
     */
    private String title;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 消息类型：1预约成功 2预约提醒 3签到成功 4违约通知 5VIP升级通知
     */
    private Integer type;
    
    /**
     * 是否已读：0未读 1已读
     */
    private Integer isRead;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}