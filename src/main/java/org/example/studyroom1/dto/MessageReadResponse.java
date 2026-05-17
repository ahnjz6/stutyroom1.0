package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 消息已读响应DTO
 */
@Data
public class MessageReadResponse {
    
    /**
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 是否已读
     */
    private Integer isRead;
    
    public MessageReadResponse() {
    }
    
    public MessageReadResponse(Long messageId, Integer isRead) {
        this.messageId = messageId;
        this.isRead = isRead;
    }
}
