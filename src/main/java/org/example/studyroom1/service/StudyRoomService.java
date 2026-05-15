package org.example.studyroom1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.studyroom1.dto.StudyRoomResponse;
import org.example.studyroom1.entity.StudyRoom;
import org.example.studyroom1.mapper.StudyRoomMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 自习室服务类
 */
@Service
@RequiredArgsConstructor
public class StudyRoomService {
    
    private final StudyRoomMapper studyRoomMapper;
    
    /**
     * 获取自习室列表（只返回启用的自习室）
     */
    public List<StudyRoomResponse> getStudyRoomList() {
        // 查询已启用的自习室，按排序优先级排序
        List<StudyRoom> studyRooms = studyRoomMapper.selectList(
            new LambdaQueryWrapper<StudyRoom>()
                .eq(StudyRoom::getStatus, 1) // 启用
                .orderByAsc(StudyRoom::getSortOrder) // 按排序优先级
        );
        
        // 转换为响应DTO
        return studyRooms.stream().map(studyRoom -> {
            StudyRoomResponse response = new StudyRoomResponse();
            BeanUtils.copyProperties(studyRoom, response);
            return response;
        }).collect(Collectors.toList());
    }
}
