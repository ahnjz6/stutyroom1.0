package org.example.studyroom1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.studyroom1.dto.AnnouncementResponse;
import org.example.studyroom1.entity.Announcement;
import org.example.studyroom1.mapper.AnnouncementMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务类
 */
@Service
@RequiredArgsConstructor
public class AnnouncementService {
    
    private final AnnouncementMapper announcementMapper;
    
    /**
     * 获取公告列表（只返回已发布且未过期的公告）
     */
    public List<AnnouncementResponse> getAnnouncementList() {
        // 查询已发布的公告，且未过期（或没有设置过期时间）
        List<Announcement> announcements = announcementMapper.selectList(
            new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1) // 已发布
                .and(wrapper -> wrapper
                    .isNull(Announcement::getExpireTime)
                    .or()
                    .gt(Announcement::getExpireTime, LocalDateTime.now())
                )
                .orderByDesc(Announcement::getIsPinned) // 置顶优先
                .orderByDesc(Announcement::getPublishTime) // 按发布时间降序
        );
        
        // 转换为响应DTO
        return announcements.stream().map(announcement -> {
            AnnouncementResponse response = new AnnouncementResponse();
            BeanUtils.copyProperties(announcement, response);
            // 将 isPinned 映射为 isTop
            response.setIsTop(announcement.getIsPinned());
            return response;
        }).collect(Collectors.toList());
    }
}
