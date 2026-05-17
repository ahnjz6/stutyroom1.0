package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 新增公告响应DTO
 */
@Data
public class CreateAnnouncementResponse {
    
    /**
     * 公告ID
     */
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 状态：0草稿 1已发布
     */
    private Integer status;
    
    public CreateAnnouncementResponse() {
    }
    
    public CreateAnnouncementResponse(Long id, String title, Integer status) {
        this.id = id;
        this.title = title;
        this.status = status;
    }
}
