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
import org.example.studyroom1.dto.QueryAvailableSeatsRequest;
import org.example.studyroom1.dto.AvailableSeatResponse;
import org.example.studyroom1.dto.BookSeatRequest;
import org.example.studyroom1.dto.BookSeatResponse;
import org.example.studyroom1.dto.MyReservationResponse;
import org.example.studyroom1.entity.Seat;
import org.example.studyroom1.entity.StudyRoom;
import org.example.studyroom1.entity.Reservation;
import org.example.studyroom1.entity.SeatOccupancy;
import org.example.studyroom1.entity.UserVipCard;
import org.example.studyroom1.entity.VipCardType;
import org.example.studyroom1.entity.Message;
import org.example.studyroom1.entity.User;
import org.example.studyroom1.entity.ViolationRecord;
import org.example.studyroom1.mapper.SeatMapper;
import org.example.studyroom1.mapper.StudyRoomMapper;
import org.example.studyroom1.mapper.ReservationMapper;
import org.example.studyroom1.mapper.SeatOccupancyMapper;
import org.example.studyroom1.mapper.SystemConfigMapper;
import org.example.studyroom1.mapper.UserVipCardMapper;
import org.example.studyroom1.mapper.VipCardTypeMapper;
import org.example.studyroom1.mapper.MessageMapper;
import org.example.studyroom1.mapper.UserMapper;
import org.example.studyroom1.mapper.ViolationRecordMapper;
import org.example.studyroom1.entity.SystemConfig;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 自习室服务类
 */
@Service
@RequiredArgsConstructor
public class StudyRoomService {
    
    private final StudyRoomMapper studyRoomMapper;
    private final SeatMapper seatMapper;
    private final ReservationMapper reservationMapper;
    private final SeatOccupancyMapper seatOccupancyMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final UserVipCardMapper userVipCardMapper;
    private final VipCardTypeMapper vipCardTypeMapper;
    private final MessageMapper messageMapper;
    private final UserMapper userMapper;
    private final ViolationRecordMapper violationRecordMapper;
    
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
    
    /**
     * 按时段查询可用座位
     */
    public List<AvailableSeatResponse> queryAvailableSeats(QueryAvailableSeatsRequest request) {
        // 参数校验
        if (request.getDate() == null || request.getDate().trim().isEmpty()) {
            throw new RuntimeException("日期不能为空");
        }
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            throw new RuntimeException("开始时间不能为空");
        }
        if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
            throw new RuntimeException("结束时间不能为空");
        }
        
        // 解析日期
        LocalDate reservationDate;
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            reservationDate = LocalDate.parse(request.getDate(), dateFormatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("日期格式错误，请使用 yyyy-MM-dd 格式");
        }
        
        // 解析时间
        LocalTime startTime;
        LocalTime endTime;
        try {
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
            endTime = LocalTime.parse(request.getEndTime(), timeFormatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("时间格式错误，请使用 HH:mm 格式");
        }
        
        // 校验时间逻辑
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }
        
        // 校验日期是否在最大提前预约天数范围内
        LocalDate today = LocalDate.now();
        if (reservationDate.isBefore(today)) {
            throw new RuntimeException("不能预约过去的日期");
        }
        
        // 获取系统配置的最大提前预约天数
        Integer maxAdvanceDays = getMaxAdvanceDays();
        LocalDate maxDate = today.plusDays(maxAdvanceDays);
        if (reservationDate.isAfter(maxDate)) {
            throw new RuntimeException("预约日期超出最大提前预约天数（" + maxAdvanceDays + "天）");
        }
        
        // 查询自习室列表（如果指定了roomId则只查询该自习室）
        List<StudyRoom> studyRooms;
        if (request.getRoomId() != null) {
            StudyRoom room = studyRoomMapper.selectById(request.getRoomId());
            if (room == null) {
                throw new RuntimeException("自习室不存在");
            }
            if (room.getStatus() != 1) {
                throw new RuntimeException("自习室已停用");
            }
            studyRooms = new ArrayList<>();
            studyRooms.add(room);
        } else {
            // 查询所有启用的自习室
            studyRooms = studyRoomMapper.selectList(
                new LambdaQueryWrapper<StudyRoom>()
                    .eq(StudyRoom::getStatus, 1)
                    .orderByAsc(StudyRoom::getId)
            );
        }
        
        // 查询所有可用座位并过滤
        List<AvailableSeatResponse> availableSeats = new ArrayList<>();
        
        for (StudyRoom room : studyRooms) {
            // 校验预约时间是否在自习室开放时间范围内
            if (startTime.isBefore(room.getOpenTime()) || endTime.isAfter(room.getCloseTime())) {
                // 该自习室的开放时间不满足预约时段要求，跳过
                continue;
            }
            
            // 查询该自习室的所有可用座位
            List<Seat> seats = seatMapper.selectList(
                new LambdaQueryWrapper<Seat>()
                    .eq(Seat::getRoomId, room.getId())
                    .eq(Seat::getStatus, 1) // 只查询可用的座位
                    .orderByAsc(Seat::getRowNumber)
                    .orderByAsc(Seat::getColNumber)
            );
            
            // 对每个座位检查是否在指定时段有预约冲突
            for (Seat seat : seats) {
                // 查询该座位在指定日期的已预约记录（状态为0待签到或1使用中）
                // 检查是否有时间冲突：已预约的时段与请求的时段有重叠
                Long conflictCount = reservationMapper.selectCount(
                    new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getSeatId, seat.getId())
                        .eq(Reservation::getReservationDate, reservationDate)
                        .in(Reservation::getStatus, 0, 1) // 0待签到 1使用中
                        .and(wrapper -> wrapper
                            // 时间冲突条件：已预约的开始时间 < 请求的结束时间 AND 已预约的结束时间 > 请求的开始时间
                            .lt(Reservation::getStartTime, endTime)
                            .gt(Reservation::getEndTime, startTime)
                        )
                );
                
                // 如果没有冲突，说明该座位在该时段可用
                if (conflictCount == 0) {
                    AvailableSeatResponse response = new AvailableSeatResponse();
                    response.setSeatId(seat.getId());
                    response.setRoomId(room.getId());
                    response.setRoomName(room.getName());
                    response.setHasPower(seat.getHasPower());
                    response.setHasWindow(seat.getIsWindow());
                    response.setStatus(1); // 可用
                    availableSeats.add(response);
                }
            }
        }
        
        return availableSeats;
    }
    
    /**
     * 获取系统配置的最大提前预约天数
     */
    private Integer getMaxAdvanceDays() {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, "maxAdvanceDays")
                .eq(SystemConfig::getIsDeleted, 0)
        );
        
        if (config != null && config.getConfigValue() != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                // 如果配置值格式错误，使用默认值7天
                return 7;
            }
        }
        
        // 如果没有配置，使用默认值7天
        return 7;
    }
    
    /**
     * 预约座位
     */
    @Transactional(rollbackFor = Exception.class)
    public BookSeatResponse bookSeat(Long userId, BookSeatRequest request) {
        // 1. 参数校验
        if (request.getSeatId() == null) {
            throw new RuntimeException("座位ID不能为空");
        }
        if (request.getDate() == null || request.getDate().trim().isEmpty()) {
            throw new RuntimeException("日期不能为空");
        }
        if (request.getStartTime() == null || request.getStartTime().trim().isEmpty()) {
            throw new RuntimeException("开始时间不能为空");
        }
        if (request.getEndTime() == null || request.getEndTime().trim().isEmpty()) {
            throw new RuntimeException("结束时间不能为空");
        }
        
        // 2. 解析日期和时间
        LocalDate reservationDate;
        LocalTime startTime;
        LocalTime endTime;
        try {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            reservationDate = LocalDate.parse(request.getDate(), dateFormatter);
            
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
            startTime = LocalTime.parse(request.getStartTime(), timeFormatter);
            endTime = LocalTime.parse(request.getEndTime(), timeFormatter);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("日期或时间格式错误");
        }
        
        // 校验时间逻辑
        if (startTime.isAfter(endTime) || startTime.equals(endTime)) {
            throw new RuntimeException("结束时间必须晚于开始时间");
        }
        
        // 3. 检查用户是否有生效的VIP卡
        UserVipCard vipCard = userVipCardMapper.selectOne(
            new LambdaQueryWrapper<UserVipCard>()
                .eq(UserVipCard::getUserId, userId)
                .eq(UserVipCard::getStatus, 1) // 状态为生效中
                .ge(UserVipCard::getEndTime, LocalDateTime.now()) // 未过期
                .orderByDesc(UserVipCard::getCreateTime)
                .last("LIMIT 1")
        );
        
        if (vipCard == null) {
            throw new RuntimeException("您还不是VIP会员，无法预约");
        }
        
        // 4. 获取VIP卡类型信息
        VipCardType cardType = vipCardTypeMapper.selectById(vipCard.getCardTypeId());
        if (cardType == null) {
            throw new RuntimeException("VIP卡类型不存在");
        }
        
        // 5. 检查是否为次卡并扣减次数
        if (cardType.getType() == 1) { // 次卡
            if (vipCard.getRemainingCount() == null || vipCard.getRemainingCount() <= 0) {
                throw new RuntimeException("您的次卡次数已用完，无法预约");
            }
            
            // 扣减次数
            vipCard.setRemainingCount(vipCard.getRemainingCount() - 1);
            
            // 如果扣减后次数为0，更新状态为已用完
            if (vipCard.getRemainingCount() == 0) {
                vipCard.setStatus(2); // 2表示已用完
            }
            
            userVipCardMapper.updateById(vipCard);
        }
        // 月卡和年卡不需要特殊处理，正常使用
        
        // 6. 查询座位信息
        Seat seat = seatMapper.selectById(request.getSeatId());
        if (seat == null) {
            throw new RuntimeException("座位不存在");
        }
        
        if (seat.getStatus() != 1) {
            throw new RuntimeException("该座位不可用");
        }
        
        // 7. 查询自习室信息以验证开放时间
        StudyRoom studyRoom = studyRoomMapper.selectById(seat.getRoomId());
        if (studyRoom == null) {
            throw new RuntimeException("自习室不存在");
        }
        
        if (studyRoom.getStatus() != 1) {
            throw new RuntimeException("自习室已停用");
        }
        
        // 校验预约时间是否在自习室开放时间范围内
        if (startTime.isBefore(studyRoom.getOpenTime()) || endTime.isAfter(studyRoom.getCloseTime())) {
            throw new RuntimeException("预约时间必须在自习室开放时间范围内（" + 
                studyRoom.getOpenTime() + "-" + studyRoom.getCloseTime() + "）");
        }
        
        // 8. 检查座位在该时间段是否已被预约
        Long seatConflictCount = reservationMapper.selectCount(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getSeatId, request.getSeatId())
                .eq(Reservation::getReservationDate, reservationDate)
                .in(Reservation::getStatus, 0, 1) // 0待签到 1使用中
                .and(wrapper -> wrapper
                    // 时间冲突条件：已预约的开始时间 < 请求的结束时间 AND 已预约的结束时间 > 请求的开始时间
                    .lt(Reservation::getStartTime, endTime)
                    .gt(Reservation::getEndTime, startTime)
                )
        );
        
        if (seatConflictCount > 0) {
            throw new RuntimeException("该座位在此时段已被预约");
        }
        
        // 9. 检查用户在该时间段是否有其他预约
        Long userConflictCount = reservationMapper.selectCount(
            new LambdaQueryWrapper<Reservation>()
                .eq(Reservation::getUserId, userId)
                .eq(Reservation::getReservationDate, reservationDate)
                .in(Reservation::getStatus, 0, 1) // 0待签到 1使用中
                .and(wrapper -> wrapper
                    // 时间冲突条件
                    .lt(Reservation::getStartTime, endTime)
                    .gt(Reservation::getEndTime, startTime)
                )
        );
        
        if (userConflictCount > 0) {
            throw new RuntimeException("该时间有其他预约信息，预约失败");
        }
        
        // 10. 创建预约记录
        Reservation reservation = new Reservation();
        reservation.setReservationNo(generateReservationNo());
        reservation.setUserId(userId);
        reservation.setSeatId(request.getSeatId());
        reservation.setRoomId(seat.getRoomId());
        reservation.setReservationDate(reservationDate);
        reservation.setStartTime(startTime);
        reservation.setEndTime(endTime);
        reservation.setDurationHours((int) (endTime.toSecondOfDay() - startTime.toSecondOfDay()) / 3600);
        reservation.setStatus(0); // 0待签到
        reservation.setVipCardId(vipCard.getId());
        reservation.setIsEarlyCheckout(0); // 默认不提前结束
        reservation.setCreateTime(LocalDateTime.now());
        reservation.setUpdateTime(LocalDateTime.now());
        
        reservationMapper.insert(reservation);
        
        // 11. 发送预约成功消息
        Message message = new Message();
        message.setUserId(userId);
        message.setMessageType(1); // 1预约成功
        message.setTitle("预约成功");
        message.setContent("您已成功预约 " + studyRoom.getName() + " " + 
            seat.getSeatNumber() + "号座位，" + 
            reservationDate + " " + startTime + "-" + endTime);
        message.setRelatedId(reservation.getId());
        message.setIsRead(0); // 未读
        message.setCreateTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        
        // 12. 返回预约结果
        BookSeatResponse response = new BookSeatResponse();
        response.setReservationId(reservation.getId());
        response.setReservationNo(reservation.getReservationNo());
        
        return response;
    }
    
    /**
     * 生成预约编号
     */
    private String generateReservationNo() {
        // 格式：RES + 年月日时分秒 + 随机4位
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 4).replace("-", "").toUpperCase();
        return "RES" + timestamp + random;
    }
    
    /**
     * 分页查询用户预约列表
     */
    public Page<MyReservationResponse> getUserReservations(Long userId, Integer page, Integer pageSize, Integer status) {
        // 构建查询条件
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUserId, userId);
        
        // 如果指定了状态，则按状态过滤
        if (status != null) {
            queryWrapper.eq(Reservation::getStatus, status);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(Reservation::getCreateTime);
        
        // 查询总数
        long total = reservationMapper.selectCount(queryWrapper);
        
        // 手动分页：计算偏移量
        long offset = (long) (page - 1) * pageSize;
        queryWrapper.last("LIMIT " + pageSize + " OFFSET " + offset);
        
        // 执行查询
        List<Reservation> reservations = reservationMapper.selectList(queryWrapper);
        
        // 转换为响应DTO
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        List<MyReservationResponse> responseList = reservations.stream().map(reservation -> {
            MyReservationResponse response = new MyReservationResponse();
            response.setId(reservation.getId());
            response.setReservationNo(reservation.getReservationNo());
            response.setDate(reservation.getReservationDate().format(dateFormatter));
            response.setStartTime(reservation.getStartTime().format(timeFormatter));
            response.setEndTime(reservation.getEndTime().format(timeFormatter));
            response.setStatus(reservation.getStatus());
            response.setCreateTime(reservation.getCreateTime());
            
            // 查询自习室名称
            StudyRoom studyRoom = studyRoomMapper.selectById(reservation.getRoomId());
            if (studyRoom != null) {
                response.setRoomName(studyRoom.getName());
            }
            
            // 查询座位号
            Seat seat = seatMapper.selectById(reservation.getSeatId());
            if (seat != null) {
                response.setSeatNo(seat.getSeatNumber());
            }
            
            return response;
        }).collect(Collectors.toList());
        
        // 构建返回的分页对象
        Page<MyReservationResponse> responsePage = new Page<>();
        responsePage.setCurrent(page);
        responsePage.setSize(pageSize);
        responsePage.setTotal(total);
        responsePage.setRecords(responseList);
        
        return responsePage;
    }
    
    /**
     * 取消预约
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelReservation(Long userId, Long reservationId) {
        // 1. 查询预约记录
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 2. 验证预约是否属于当前用户
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权取消该预约");
        }
        
        // 3. 验证预约状态
        if (reservation.getStatus() != 0) {
            throw new RuntimeException("只有待签到的预约才能取消");
        }
        
        // 4. 验证是否还未到预约时间
        LocalDateTime reservationDateTime = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());
        LocalDateTime now = LocalDateTime.now();
        
        if (now.isAfter(reservationDateTime)) {
            throw new RuntimeException("预约时间已开始，无法取消");
        }
        
        // 5. 如果是次卡，归还次数
        UserVipCard vipCard = userVipCardMapper.selectById(reservation.getVipCardId());
        if (vipCard != null) {
            VipCardType cardType = vipCardTypeMapper.selectById(vipCard.getCardTypeId());
            if (cardType != null && cardType.getType() == 1) { // 次卡
                // 归还次数
                vipCard.setRemainingCount(vipCard.getRemainingCount() + 1);
                // 如果状态是已用完(2)，改为生效中(1)
                if (vipCard.getStatus() == 2) {
                    vipCard.setStatus(1);
                }
                userVipCardMapper.updateById(vipCard);
            }
        }
        
        // 6. 更新预约状态为已取消
        reservation.setStatus(3); // 3表示已取消
        reservation.setCancelTime(now);
        reservation.setUpdateTime(now);
        reservationMapper.updateById(reservation);
        
        // 7. 发送取消预约消息
        StudyRoom studyRoom = studyRoomMapper.selectById(reservation.getRoomId());
        Seat seat = seatMapper.selectById(reservation.getSeatId());
        
        if (studyRoom != null && seat != null) {
            Message message = new Message();
            message.setUserId(userId);
            message.setMessageType(3); // 消息类型：取消预约
            message.setTitle("预约已取消");
            message.setContent("您已取消 " + studyRoom.getName() + " " + 
                seat.getSeatNumber() + "号座位的预约");
            message.setRelatedId(reservationId);
            message.setIsRead(0); // 未读
            message.setCreateTime(now);
            messageMapper.insert(message);
        }
    }
    
    /**
     * 签到
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkin(Long userId, Long reservationId) {
        // 1. 查询预约记录
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 2. 验证预约是否属于当前用户
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权签到该预约");
        }
        
        // 3. 验证预约状态
        if (reservation.getStatus() != 0) {
            throw new RuntimeException("只有待签到的预约才能签到");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationStartTime = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());
        
        // 4. 获取系统配置
        Integer checkInWindow = getCheckInWindow(); // 签到时间窗口（分钟）
        Integer checkInBeforeMinutes = getCheckInBeforeMinutes(); // 可提前签到分钟数
        
        // 5. 计算最早和最晚签到时间
        LocalDateTime earliestCheckinTime = reservationStartTime.minusMinutes(checkInBeforeMinutes);
        LocalDateTime latestCheckinTime = reservationStartTime.plusMinutes(checkInWindow);
        
        // 6. 判断签到时间是否合法
        if (now.isBefore(earliestCheckinTime)) {
            throw new RuntimeException("签到时间未到，请在预约开始前" + checkInBeforeMinutes + "分钟内签到");
        }
        
        if (now.isAfter(latestCheckinTime)) {
            // 超过签到时间窗口，记为违约
            handleViolation(userId, reservation, now);
            throw new RuntimeException("签到超时，已记一次违约");
        }
        
        // 7. 更新预约状态为使用中
        reservation.setStatus(1); // 1表示使用中
        reservation.setCheckinTime(now);
        reservation.setUpdateTime(now);
        reservationMapper.updateById(reservation);
        
        // 8. 发送签到成功消息
        StudyRoom studyRoom = studyRoomMapper.selectById(reservation.getRoomId());
        Seat seat = seatMapper.selectById(reservation.getSeatId());
        
        if (studyRoom != null && seat != null) {
            Message message = new Message();
            message.setUserId(userId);
            message.setMessageType(3); // 3表示签到成功
            message.setTitle("签到成功");
            message.setContent("您已成功签到 " + studyRoom.getName() + " " + 
                seat.getSeatNumber() + "号座位，预约时段：" + 
                reservation.getReservationDate() + " " + 
                reservation.getStartTime() + "-" + reservation.getEndTime());
            message.setRelatedId(reservationId);
            message.setIsRead(0); // 未读
            message.setCreateTime(now);
            messageMapper.insert(message);
        }
    }
    
    /**
     * 签退
     */
    @Transactional(rollbackFor = Exception.class)
    public void checkout(Long userId, Long reservationId) {
        // 1. 查询预约记录
        Reservation reservation = reservationMapper.selectById(reservationId);
        if (reservation == null) {
            throw new RuntimeException("预约记录不存在");
        }
        
        // 2. 验证预约是否属于当前用户
        if (!reservation.getUserId().equals(userId)) {
            throw new RuntimeException("无权签退该预约");
        }
        
        // 3. 验证预约状态（只有使用中才能签退）
        if (reservation.getStatus() != 1) {
            throw new RuntimeException("只有使用中的预约才能签退");
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reservationEndTime = LocalDateTime.of(reservation.getReservationDate(), reservation.getEndTime());
        
        // 4. 判断是正常签退还是提前签退
        boolean isEarlyCheckout = now.isBefore(reservationEndTime);
        
        if (isEarlyCheckout) {
            // 提前签退：需要释放剩余时段
            handleEarlyCheckout(userId, reservation, now);
        } else {
            // 正常签退：到达或超过结束时间
            reservation.setStatus(2); // 2表示已完成
            reservation.setCheckoutTime(now);
            reservation.setIsEarlyCheckout(0);
            reservation.setUpdateTime(now);
            reservationMapper.updateById(reservation);
        }
        
        // 5. 释放座位占用时段（将状态从2使用中改为0空闲）
        releaseSeatOccupancy(reservation, now);
        
        // 6. 如果是次卡，扣除使用次数
        if (reservation.getVipCardId() != null) {
            UserVipCard vipCard = userVipCardMapper.selectById(reservation.getVipCardId());
            if (vipCard != null) {
                VipCardType cardType = vipCardTypeMapper.selectById(vipCard.getCardTypeId());
                if (cardType != null && cardType.getType() == 1) {
                    // 次卡：扣除次数
                    vipCard.setRemainingCount(vipCard.getRemainingCount() - 1);
                    if (vipCard.getRemainingCount() <= 0) {
                        vipCard.setStatus(2); // 已用完
                    }
                    vipCard.setUpdateTime(now);
                    userVipCardMapper.updateById(vipCard);
                }
            }
        }
        
        // 7. 发送签退成功消息
        StudyRoom studyRoom = studyRoomMapper.selectById(reservation.getRoomId());
        Seat seat = seatMapper.selectById(reservation.getSeatId());
        
        if (studyRoom != null && seat != null) {
            Message message = new Message();
            message.setUserId(userId);
            message.setMessageType(6); // 6表示签退成功
            message.setTitle("签退成功");
            message.setContent("您已签退 " + studyRoom.getName() + " " + 
                seat.getSeatNumber() + "号座位" +
                (isEarlyCheckout ? "（提前签退）" : ""));
            message.setRelatedId(reservationId);
            message.setIsRead(0); // 未读
            message.setCreateTime(now);
            messageMapper.insert(message);
        }
    }
    
    /**
     * 处理提前签退：释放签退时间之后的时段
     */
    private void handleEarlyCheckout(Long userId, Reservation reservation, LocalDateTime checkoutTime) {
        // 计算签退时间的下一个整点小时
        int checkoutHour = checkoutTime.getHour();
        // 签退时间往后的时间取整，即从下一个整点开始释放
        int releaseStartHour = checkoutHour + 1;
        
        // 预约结束时间
        int endTimeHour = reservation.getEndTime().getHour();
        
        // 释放从 releaseStartHour 到 endTimeHour 之间的所有时段
        LocalDate reservationDate = reservation.getReservationDate();
        
        for (int hour = releaseStartHour; hour < endTimeHour; hour++) {
            // 查询该时段的占用记录
            LambdaQueryWrapper<SeatOccupancy> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(SeatOccupancy::getSeatId, reservation.getSeatId())
                       .eq(SeatOccupancy::getOccupancyDate, reservationDate)
                       .eq(SeatOccupancy::getHourSlot, hour)
                       .eq(SeatOccupancy::getReservationId, reservation.getId());
            
            SeatOccupancy occupancy = seatOccupancyMapper.selectOne(queryWrapper);
            if (occupancy != null) {
                // 释放该时段：状态改为空闲，清空预约ID
                occupancy.setStatus(0); // 0表示空闲
                occupancy.setReservationId(null);
                occupancy.setUpdateTime(checkoutTime);
                seatOccupancyMapper.updateById(occupancy);
            }
        }
        
        // 更新预约记录
        reservation.setStatus(2); // 2表示已完成
        reservation.setCheckoutTime(checkoutTime);
        reservation.setIsEarlyCheckout(1); // 1表示提前签退
        reservation.setUpdateTime(checkoutTime);
        reservationMapper.updateById(reservation);
    }
    
    /**
     * 释放座位占用时段（签退时使用）
     */
    private void releaseSeatOccupancy(Reservation reservation, LocalDateTime updateTime) {
        // 查询该预约的所有占用记录
        LambdaQueryWrapper<SeatOccupancy> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SeatOccupancy::getSeatId, reservation.getSeatId())
                   .eq(SeatOccupancy::getOccupancyDate, reservation.getReservationDate())
                   .eq(SeatOccupancy::getReservationId, reservation.getId());
        
        List<SeatOccupancy> occupancies = seatOccupancyMapper.selectList(queryWrapper);
        
        // 释放所有时段
        for (SeatOccupancy occupancy : occupancies) {
            occupancy.setStatus(0); // 0表示空闲
            occupancy.setReservationId(null); // 清空预约ID
            occupancy.setUpdateTime(updateTime);
            seatOccupancyMapper.updateById(occupancy);
        }
    }
    
    /**
     * 处理违约
     */
    private void handleViolation(Long userId, Reservation reservation, LocalDateTime violationTime) {
        // 1. 更新预约状态为已违约
        reservation.setStatus(4); // 4表示已违约
        reservation.setUpdateTime(violationTime);
        reservationMapper.updateById(reservation);
        
        // 2. 创建违约记录
        ViolationRecord violationRecord = new ViolationRecord();
        violationRecord.setUserId(userId);
        violationRecord.setReservationId(reservation.getId());
        violationRecord.setViolationType(1); // 1表示超时未签到
        violationRecord.setViolationTime(violationTime);
        violationRecord.setAppealStatus(0); // 0表示未申诉
        violationRecord.setCreateTime(violationTime);
        violationRecord.setUpdateTime(violationTime);
        
        violationRecordMapper.insert(violationRecord);
        
        // 3. 更新用户违约次数
        User user = userMapper.selectById(userId);
        if (user != null) {
            // 检查是否需要重置违约次数（7天未重置）
            LocalDate today = LocalDate.now();
            if (user.getLastViolationReset() == null || 
                user.getLastViolationReset().isBefore(today.minusDays(7))) {
                // 超过7天，重置违约次数
                user.setViolationCount(0);
                user.setLastViolationReset(today);
            }
            
            // 增加违约次数
            user.setViolationCount((user.getViolationCount() == null ? 0 : user.getViolationCount()) + 1);
            
            // 检查是否达到封禁阈值
            Integer violationLimit = getViolationLimit();
            if (user.getViolationCount() >= violationLimit) {
                // 违约次数达到限制，封禁用户
                user.setStatus(0); // 0表示封禁
            }
            
            user.setUpdateTime(violationTime);
            userMapper.updateById(user);
            
            // 4. 发送违约通知消息
            StudyRoom studyRoom = studyRoomMapper.selectById(reservation.getRoomId());
            Seat seat = seatMapper.selectById(reservation.getSeatId());
            
            if (studyRoom != null && seat != null) {
                Message message = new Message();
                message.setUserId(userId);
                message.setMessageType(4); // 4表示违约通知
                message.setTitle("违约通知");
                message.setContent("您因未按时签到，已在 " + studyRoom.getName() + " " + 
                    seat.getSeatNumber() + "号座位产生一次违约。" +
                    "当前违约次数：" + user.getViolationCount() + "/" + violationLimit + "。" +
                    (user.getStatus() == 0 ? "您的账号已被封禁。" : ""));
                message.setRelatedId(reservation.getId());
                message.setIsRead(0); // 未读
                message.setCreateTime(violationTime);
                messageMapper.insert(message);
            }
        }
    }
    
    /**
     * 获取签到时间窗口（分钟）
     */
    private Integer getCheckInWindow() {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, "checkInWindow")
                .eq(SystemConfig::getIsDeleted, 0)
        );
        
        if (config != null && config.getConfigValue() != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                return 10; // 默认10分钟
            }
        }
        
        return 10; // 默认10分钟
    }
    
    /**
     * 获取可提前签到分钟数
     */
    private Integer getCheckInBeforeMinutes() {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, "checkInBeforeMinutes")
                .eq(SystemConfig::getIsDeleted, 0)
        );
        
        if (config != null && config.getConfigValue() != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                return 10; // 默认10分钟
            }
        }
        
        return 10; // 默认10分钟
    }
    
    /**
     * 获取违约封禁次数限制
     */
    private Integer getViolationLimit() {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, "violationLimit")
                .eq(SystemConfig::getIsDeleted, 0)
        );
        
        if (config != null && config.getConfigValue() != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                return 3; // 默认3次
            }
        }
        
        return 3; // 默认3次
    }
    
    /**
     * 获取系统配置的默认预约次数
     */
    private Integer getDefaultReservationCount() {
        SystemConfig config = systemConfigMapper.selectOne(
            new LambdaQueryWrapper<SystemConfig>()
                .eq(SystemConfig::getConfigKey, "defaultReservationCount")
                .eq(SystemConfig::getIsDeleted, 0)
        );
        
        if (config != null && config.getConfigValue() != null) {
            try {
                return Integer.parseInt(config.getConfigValue());
            } catch (NumberFormatException e) {
                // 如果配置值格式错误，使用默认值3次
                return 3;
            }
        }
        
        // 如果没有配置，使用默认值3次
        return 3; // 默认3次
    }
}
