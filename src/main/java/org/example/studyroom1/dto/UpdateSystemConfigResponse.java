package org.example.studyroom1.dto;

import lombok.Data;

/**
 * 修改系统配置响应DTO
 */
@Data
public class UpdateSystemConfigResponse {
    
    /**
     * 更新成功的配置项数量
     */
    private Integer updateCount;
    
    /**
     * 更新时间
     */
    private String updateTime;
    
    public UpdateSystemConfigResponse() {
    }
    
    public UpdateSystemConfigResponse(Integer updateCount, String updateTime) {
        this.updateCount = updateCount;
        this.updateTime = updateTime;
    }
}
