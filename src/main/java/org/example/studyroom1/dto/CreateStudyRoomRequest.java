package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 新增自习室请求DTO
 */
@Data
public class CreateStudyRoomRequest {
    
    /**
     * 自习室名称
     */
    private String name;
    
    /**
     * 位置
     */
    private String location;
    
    /**
     * 开放时间
     */
    private String openTime;
    
    /**
     * 关闭时间
     */
    private String closeTime;
    
    /**
     * 总行数
     */
    private Integer totalRow;
    
    /**
     * 总列数
     */
    private Integer totalCol;
}
