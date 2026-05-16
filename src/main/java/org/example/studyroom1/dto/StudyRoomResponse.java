package org.example.studyroom1.dto;

import lombok.Data;

import java.time.LocalTime;

/**
 * 自习室响应DTO
 */
@Data
public class StudyRoomResponse {
    
    private Long id;
    private String name;
    private String building;
    private String floor;
    private String locationDesc;
    private LocalTime openTime;
    private LocalTime closeTime;
    private Integer totalSeats;
    private Integer quietLevel;
    private Integer sortOrder;
}
