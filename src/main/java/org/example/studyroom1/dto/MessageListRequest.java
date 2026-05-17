package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 消息列表请求DTO
 */
@Data
public class MessageListRequest {
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页大小
     */
    private Integer pageSize;
}