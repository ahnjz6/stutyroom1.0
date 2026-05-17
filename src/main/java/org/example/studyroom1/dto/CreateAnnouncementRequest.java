package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 新增公告请求DTO
 */
@Data
public class CreateAnnouncementRequest {
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告内容
     */
    private String content;
    
    /**
     * 是否置顶：0否 1是
     */
    private Integer isTop;
    
    /**
     * 过期时间
     */
    private String expireTime;
}
