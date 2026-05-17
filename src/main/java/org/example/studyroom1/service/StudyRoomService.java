package org.example.studyroom1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.studyroom1.dto.AdminStudyRoomResponse;
import org.example.studyroom1.dto.UserStudyRoomResponse;
import org.example.studyroom1.dto.CreateStudyRoomRequest;
import org.example.studyroom1.dto.SeatResponse;
import org.example.studyroom1.dto.StudyRoomResponse;
import org.example.studyroom1.dto.UserSeatResponse;
import org.example.studyroom1.dto.SeatDetailResponse;
import org.example.studyroom1.dto.TimeSlotDTO;
import org.example.studyroom1.dto.UpdateSeatStatusRequest;
import org.example.studyroom1.entity.Seat;
import org.example.studyroom1.entity.StudyRoom;
import org.example.studyroom1.entity.Reservation;
import org.example.studyroom1.mapper.SeatMapper;
import org.example.studyroom1.mapper.StudyRoomMapper;
import org.example.studyroom1.mapper.ReservationMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 自习室服务类
 */
@Service
@RequiredArgsConstructor
public class StudyRoomService {
    
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final ReservationMapper reservationMapper;
    
    /**
     * 获取自习室列表（只返回启用的自习室）
     */
    public List<StudyRoomResponse> getStudyRoomList() {
        // 查询已启用的自习室
        List<StudyRoom> studyRooms = studyRoomMapper.selectList(
            new LambdaQueryWrapper<StudyRoom>()
                .eq(StudyRoom::getStatus, 1) // 启用
                .orderByAsc(StudyRoom::getId) // 按ID排序
        );
        
        // 转换为响应DTO
        return studyRooms.stream().map(studyRoom -> {
            StudyRoomResponse response = new StudyRoomResponse();
            BeanUtils.copyProperties(studyRoom, response);
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取自习室列表（用户端，只返回id和name）
     */
    public List<UserStudyRoomResponse> getUserStudyRoomList() {
        // 查询已启用的自习室
        List<StudyRoom> studyRooms = studyRoomMapper.selectList(
            new LambdaQueryWrapper<StudyRoom>()
                .eq(StudyRoom::getStatus, 1) // 启用
                .orderByAsc(StudyRoom::getId) // 按ID排序
        );
        
        // 转换为响应DTO
        return studyRooms.stream().map(studyRoom -> {
            UserStudyRoomResponse response = new UserStudyRoomResponse();
            response.setId(studyRoom.getId());
            response.setName(studyRoom.getName());
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * 新增自习室
     */
    public StudyRoom createStudyRoom(CreateStudyRoomRequest request) {
        // 检查自习室名称是否已存在
        Long count = studyRoomMapper.selectCount(
            new LambdaQueryWrapper<StudyRoom>()
                .eq(StudyRoom::getName, request.getName())
        );
        if (count > 0) {
            throw new RuntimeException("自习室已存在");
        }
        
        // 解析时间字符串为LocalTime
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime openTime = LocalTime.parse(request.getOpenTime(), formatter);
        LocalTime closeTime = LocalTime.parse(request.getCloseTime(), formatter);
        
        // 计算总座位数
        Integer totalSeats = request.getTotalRow() * request.getTotalCol();
        
        // 创建自习室实体
        StudyRoom studyRoom = new StudyRoom();
        studyRoom.setName(request.getName());
        studyRoom.setLocationDesc(request.getLocation());
        studyRoom.setOpenTime(openTime);
        studyRoom.setCloseTime(closeTime);
        studyRoom.setTotalSeats(totalSeats);
        studyRoom.setStatus(1); // 默认启用
        
        // 插入自习室到数据库
        studyRoomMapper.insert(studyRoom);
        
        // 创建所有座位
        for (int row = 1; row <= request.getTotalRow(); row++) {
            for (int col = 1; col <= request.getTotalCol(); col++) {
                Seat seat = new Seat();
                seat.setRoomId(studyRoom.getId());
                seat.setSeatNumber(row + "-" + col); // 座位编号：行-列
                seat.setRowNumber(row);
                seat.setColNumber(col);
                seat.setHasPower(1); // 默认有电源
                seat.setIsWindow(0); // 默认不靠窗
                seat.setStatus(1); // 默认可用
                
                seatMapper.insert(seat);
            }
        }
        
        return studyRoom;
    }
    
    /**
     * 修改自习室状态
     */
    public void updateStudyRoomStatus(Long id, Integer status) {
        // 查询自习室是否存在
        StudyRoom studyRoom = studyRoomMapper.selectById(id);
        if (studyRoom == null) {
            throw new RuntimeException("自习室不存在");
        }
        
        // 更新状态
        studyRoom.setStatus(status);
        studyRoomMapper.updateById(studyRoom);
    }
    
    /**
     * 获取自习室列表（管理员端，返回所有自习室）
     */
    public List<AdminStudyRoomResponse> getAdminStudyRoomList() {
        // 查询所有自习室
        List<StudyRoom> studyRooms = studyRoomMapper.selectList(
            new LambdaQueryWrapper<StudyRoom>()
                .orderByAsc(StudyRoom::getId)
        );
        
        // 转换为响应DTO
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return studyRooms.stream().map(studyRoom -> {
            AdminStudyRoomResponse response = new AdminStudyRoomResponse();
            response.setId(studyRoom.getId());
            response.setName(studyRoom.getName());
            response.setLocation(studyRoom.getLocationDesc());
            response.setOpenTime(studyRoom.getOpenTime().format(formatter));
            response.setCloseTime(studyRoom.getCloseTime().format(formatter));
            response.setTotalSeats(studyRoom.getTotalSeats());
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取指定自习室的座位列表
     */
    public List<SeatResponse> getSeatListByRoomId(Long roomId) {
        // 查询自习室是否存在
        StudyRoom studyRoom = studyRoomMapper.selectById(roomId);
        if (studyRoom == null) {
            throw new RuntimeException("自习室不存在");
        }
        
        // 查询该自习室的所有座位，按行号和列号排序
        List<Seat> seats = seatMapper.selectList(
            new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, roomId)
                .orderByAsc(Seat::getRowNumber)
                .orderByAsc(Seat::getColNumber)
        );
        
        // 转换为响应DTO
        return seats.stream().map(seat -> {
            SeatResponse response = new SeatResponse();
            response.setSeatId(seat.getId());
            response.setRow(seat.getRowNumber());
            response.setCol(seat.getColNumber());
            response.setStatus(seat.getStatus());
            response.setHasPower(seat.getHasPower());
            response.setHasWindow(seat.getIsWindow());
            // 已预约时段列表暂时返回空数组，后续可根据预约数据填充
            response.setBookedSlots(new ArrayList<>());
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * 获取指定自习室的座位平面图（用户端）
     */
    public List<UserSeatResponse> getUserSeatList(Long roomId) {
        // 查询自习室是否存在
        StudyRoom studyRoom = studyRoomMapper.selectById(roomId);
        if (studyRoom == null) {
            throw new RuntimeException("自习室不存在");
        }
        
        // 查询该自习室的所有座位，按行号和列号排序
        List<Seat> seats = seatMapper.selectList(
            new LambdaQueryWrapper<Seat>()
                .eq(Seat::getRoomId, roomId)
                .orderByAsc(Seat::getRowNumber)
                .orderByAsc(Seat::getColNumber)
        );
        
        // 转换为响应DTO（仅包含平面图所需字段）
        return seats.stream().map(seat -> {
            UserSeatResponse response = new UserSeatResponse();
            response.setSeatId(seat.getId());
            response.setRow(seat.getRowNumber());
            response.setCol(seat.getColNumber());
            response.setStatus(seat.getStatus());
            return response;
        }).collect(Collectors.toList());
    }
    
    /**
     * 修改座位信息
     */
    public void updateSeatStatus(UpdateSeatStatusRequest request) {
        // 查询座位是否存在
        Seat seat = seatMapper.selectById(request.getSeatId());
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        
        // 更新座位信息
        if (request.getHasPower() != null) {
            seat.setHasPower(request.getHasPower());
        }
        if (request.getHasWindow() != null) {
            seat.setIsWindow(request.getHasWindow());
        }
        if (request.getStatus() != null) {
            seat.setStatus(request.getStatus());
        }
        
        // 保存到数据库
        seatMapper.updateById(seat);
    }
    
    /**
     * 获取座位详情（用户端）
     */
    public SeatDetailResponse getSeatDetail(Integer seatId, String date) {
        // 查询座位信息
        Seat seat = seatMapper.selectById(seatId);
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        
        // 查询自习室信息以获取开放时间
        StudyRoom studyRoom = studyRoomMapper.selectById(seat.getRoomId());
        if (studyRoom == null) {
            throw new RuntimeException("所属自习室不存在");
        }
        
        // 构建响应
        SeatDetailResponse response = new SeatDetailResponse();
        response.setHasPower(seat.getHasPower());
        response.setHasWindow(seat.getIsWindow());
        
        LocalTime openTime = studyRoom.getOpenTime();
        LocalTime closeTime = studyRoom.getCloseTime();
        response.setStartTime(openTime.toString());
        response.setEndTime(closeTime.toString());
        
        // 查询该座位在指定日期的已预约记录（状态为0待签到或1使用中）
        List<TimeSlotDTO> slots = new ArrayList<>();
        if (openTime != null && closeTime != null && date != null) {
            // 使用自定义格式化器支持多种日期格式（如 2026-5-17 或 2026-05-17）
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-M-d");
            LocalDate reservationDate = LocalDate.parse(date, dateFormatter);
            
            List<Reservation> reservations = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                    .eq(Reservation::getSeatId, seatId)
                    .eq(Reservation::getReservationDate, reservationDate)
                    .in(Reservation::getStatus, 0, 1) // 0待签到 1使用中
                    .orderByAsc(Reservation::getStartTime)
            );
            
            // 计算可用时段（取补集）
            LocalTime current = openTime;
            for (Reservation res : reservations) {
                LocalTime bookStart = res.getStartTime();
                LocalTime bookEnd = res.getEndTime();
                
                // 如果预约开始时间晚于当前时间，说明中间有一段空闲
                if (bookStart.isAfter(current)) {
                    TimeSlotDTO slot = new TimeSlotDTO();
                    slot.setStart(current.toString());
                    slot.setEnd(bookStart.toString());
                    slots.add(slot);
                }
                
                // 更新当前时间为预约结束时间（取较大值）
                if (bookEnd.isAfter(current)) {
                    current = bookEnd;
                }
            }
            
            // 最后一个预约结束后到关闭时间还有一段空闲
            if (current.isBefore(closeTime)) {
                TimeSlotDTO slot = new TimeSlotDTO();
                slot.setStart(current.toString());
                slot.setEnd(closeTime.toString());
                slots.add(slot);
            }
        }
        response.setSlots(slots);
        
        return response;
    }
}
