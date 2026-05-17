package org.example.studyroom1.dto;

import lombok.Data;
import java.util.List;

@Data
public class SeatDetailResponse {
    private Integer hasPower;
    private Integer hasWindow;
    private String startTime;
    private String endTime;
    private List<TimeSlotDTO> slots;
}
