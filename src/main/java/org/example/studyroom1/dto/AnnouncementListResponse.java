package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 公告列表响应DTO
 */
@Data
public class AnnouncementListResponse {
    
    /**
     * 公告ID
     */
    private Long id;
    
    /**
     * 公告标题
     */
    private String title;
    
    /**
     * 公告正文
     */
    private String content;
    
    /**
     * 是否置顶：0否 1是
     */
    private Integer isTop;
    
    /**
     * 状态：0草稿 1已发布 2已下架
     */
    private Integer status;
    
    /**
     * 过期时间
     */
    private String expireTime;
    
    /**
     * 创建时间
     */
    private String createTime;
}
