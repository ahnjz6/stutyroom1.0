package org.example.studyroom1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.example.studyroom1.dto.LoginRequest;
import org.example.studyroom1.dto.LoginResponse;
import org.example.studyroom1.dto.MessageListRequest;
import org.example.studyroom1.dto.MessageReadResponse;
import org.example.studyroom1.dto.MessageResponse;
import org.example.studyroom1.dto.MyVipInfoResponse;
import org.example.studyroom1.dto.PageResponse;
import org.example.studyroom1.dto.PurchaseVipRequest;
import org.example.studyroom1.dto.PurchaseVipResponse;
import org.example.studyroom1.dto.VipCardListResponse;
import org.example.studyroom1.entity.Message;
import org.example.studyroom1.entity.User;
import org.example.studyroom1.entity.UserLoginLog;
import org.example.studyroom1.entity.UserVipCard;
import org.example.studyroom1.entity.VipCardType;
import org.example.studyroom1.entity.VipPurchaseRecord;
import org.example.studyroom1.mapper.MessageMapper;
import org.example.studyroom1.mapper.UserLoginLogMapper;
import org.example.studyroom1.mapper.UserMapper;
import org.example.studyroom1.mapper.UserVipCardMapper;
import org.example.studyroom1.mapper.VipCardTypeMapper;
import org.example.studyroom1.mapper.VipPurchaseRecordMapper;
import org.example.studyroom1.util.JwtUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务类
 */
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserMapper userMapper;
    private final UserLoginLogMapper userLoginLogMapper;
    private final UserVipCardMapper userVipCardMapper;
    private final MessageMapper messageMapper;
    private final VipCardTypeMapper vipCardTypeMapper;
    private final VipPurchaseRecordMapper vipPurchaseRecordMapper;
    
    /**
     * 用户登录
     */
    public LoginResponse login(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        
        // 查询用户
        User user = userMapper.selectOne(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
        );
        
        if (user == null) {
            // 记录登录失败日志
            recordLoginLog(null, username, 0, "用户不存在");
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查账号状态
        if (user.getStatus() == 0) {
            recordLoginLog(user.getId(), username, 0, "账号已被封禁");
            throw new RuntimeException("账号已被封禁，请联系管理员");
        }
        
        // 检查账号是否锁定
        if (user.getLockTime() != null && user.getLockTime().isAfter(LocalDateTime.now())) {
            recordLoginLog(user.getId(), username, 0, "账号已锁定");
            throw new RuntimeException("账号已锁定，请稍后再试");
        }
        
        // 验证密码（明文比对）
        if (!password.equals(user.getPassword())) {
            // 更新登录失败次数
            updateLoginFailCount(user);
            recordLoginLog(user.getId(), username, 0, "密码错误");
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 登录成功，重置失败次数
        user.setLoginFailCount(0);
        user.setLockTime(null);
        userMapper.updateById(user);
        
        // 生成JWT Token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        
        // 检查是否为VIP
        Integer isVip = checkVipStatus(user.getId());
        
        // 记录登录成功日志
        recordLoginLog(user.getId(), username, 1, null);
        
        // 构建响应
        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setToken(token);
        response.setStatus(user.getStatus());
        response.setIsVip(isVip);
        
        return response;
    }
    
    /**
     * 更新登录失败次数
     */
    private void updateLoginFailCount(User user) {
        user.setLoginFailCount(user.getLoginFailCount() + 1);
        
        // 如果连续失败5次，锁定账号30分钟
        if (user.getLoginFailCount() >= 5) {
            user.setLockTime(LocalDateTime.now().plusMinutes(30));
        }
        
        userMapper.updateById(user);
    }
    
    /**
     * 检查VIP状态
     */
    private Integer checkVipStatus(Long userId) {
        // 查询用户是否有生效的VIP卡
        Long count = userVipCardMapper.selectCount(
            new LambdaQueryWrapper<UserVipCard>()
                .eq(UserVipCard::getUserId, userId)
                .eq(UserVipCard::getStatus, 1) // 生效中
                .le(UserVipCard::getStartTime, LocalDateTime.now())
                .ge(UserVipCard::getEndTime, LocalDateTime.now())
        );
        
        return count > 0 ? 1 : 0;
    }
    
    /**
     * 记录登录日志
     */
    private void recordLoginLog(Long userId, String username, Integer status, String failReason) {
        UserLoginLog log = new UserLoginLog();
        log.setUserId(userId);
        log.setLoginTime(LocalDateTime.now());
        log.setStatus(status);
        log.setFailReason(failReason);
        // TODO: 从请求中获取IP和User-Agent
        log.setLoginIp("127.0.0.1");
        log.setUserAgent("Unknown");
        
        userLoginLogMapper.insert(log);
    }
    
    /**
     * 获取用户消息列表（分页）
     */
    public PageResponse<MessageResponse> getMessageList(Long userId, MessageListRequest request) {
        // 参数校验
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        if (request.getPageSize() == null || request.getPageSize() < 1) {
            request.setPageSize(10);
        }
        
        // 创建分页对象
        Page<Message> page = new Page<>(request.getPage(), request.getPageSize());
        
        // 查询当前用户的消息，按创建时间倒序排列
        Page<Message> messagePage = messageMapper.selectPage(
            page,
            new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .orderByDesc(Message::getCreateTime)
        );
        
        // 转换为响应DTO
        List<MessageResponse> messageResponses = messagePage.getRecords().stream()
            .map(this::convertToMessageResponse)
            .collect(Collectors.toList());
        
        // 构建分页响应
        return new PageResponse<>(
            messagePage.getTotal(),
            (int) messagePage.getCurrent(),
            (int) messagePage.getSize(),
            messageResponses
        );
    }
    
    /**
     * 转换Message实体为MessageResponse DTO
     */
    private MessageResponse convertToMessageResponse(Message message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setTitle(message.getTitle());
        response.setContent(message.getContent());
        response.setType(message.getMessageType());
        response.setIsRead(message.getIsRead());
        response.setCreateTime(message.getCreateTime());
        return response;
    }
    
    /**
     * 获取VIP卡列表
     */
    public List<VipCardListResponse> getVipCardList() {
        // 查询所有上架的VIP卡类型，按排序字段升序排列
        List<VipCardType> vipCardTypes = vipCardTypeMapper.selectList(
            new LambdaQueryWrapper<VipCardType>()
                .eq(VipCardType::getStatus, 1) // 只查询上架的
                .orderByAsc(VipCardType::getSortOrder)
        );
        
        // 转换为响应DTO
        return vipCardTypes.stream()
            .map(this::convertToVipCardListResponse)
            .collect(Collectors.toList());
    }
    
    /**
     * 转换VipCardType实体为VipCardListResponse DTO
     */
    private VipCardListResponse convertToVipCardListResponse(VipCardType vipCardType) {
        VipCardListResponse response = new VipCardListResponse();
        response.setId(vipCardType.getId());
        response.setName(vipCardType.getName());
        response.setType(vipCardType.getType());
        response.setPrice(vipCardType.getPrice());
        response.setMaxBookingHours(vipCardType.getMaxReservationHours());
        response.setAdvanceDays(vipCardType.getAdvanceDays());
        response.setViolationLimit(vipCardType.getViolationThreshold());
        
        // 根据卡类型设置duration字段
        if (vipCardType.getType() == 1) {
            // 次卡：duration表示可用次数
            response.setDuration(vipCardType.getUsageCount());
        } else {
            // 月卡/年卡：duration表示有效期天数
            response.setDuration(vipCardType.getDurationDays());
        }
        
        return response;
    }
    
    /**
     * 将消息标记为已读
     */
    public MessageReadResponse markMessageAsRead(Long userId, Long messageId) {
        // 查询消息
        Message message = messageMapper.selectById(messageId);
        
        if (message == null) {
            throw new RuntimeException("消息不存在");
        }
        
        // 验证消息是否属于当前用户
        if (!message.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作此消息");
        }
        
        // 如果已经是已读状态，直接返回
        if (message.getIsRead() == 1) {
            return new MessageReadResponse(messageId, 1);
        }
        
        // 更新为已读状态
        message.setIsRead(1);
        message.setReadTime(LocalDateTime.now());
        messageMapper.updateById(message);
        
        return new MessageReadResponse(messageId, 1);
    }
    
    /**
     * 获取用户VIP信息
     */
    public MyVipInfoResponse getMyVipInfo(Long userId) {
        MyVipInfoResponse response = new MyVipInfoResponse();
        
        // 查询用户当前生效的VIP卡
        UserVipCard userVipCard = userVipCardMapper.selectOne(
            new LambdaQueryWrapper<UserVipCard>()
                .eq(UserVipCard::getUserId, userId)
                .eq(UserVipCard::getStatus, 1) // 生效中
                .le(UserVipCard::getStartTime, LocalDateTime.now())
                .ge(UserVipCard::getEndTime, LocalDateTime.now())
                .orderByDesc(UserVipCard::getEndTime)
                .last("LIMIT 1")
        );
        
        if (userVipCard == null) {
            // 用户没有生效的VIP卡
            response.setIsVip(0);
            response.setCardName("");
            response.setExpireTime(null);
            response.setRemainingTimes(0);
            response.setLevel(0);
        } else {
            // 用户有生效的VIP卡
            response.setIsVip(1);
            response.setExpireTime(userVipCard.getEndTime());
            response.setRemainingTimes(userVipCard.getRemainingCount() != null ? userVipCard.getRemainingCount() : 0);
            
            // 查询VIP卡类型信息
            VipCardType vipCardType = vipCardTypeMapper.selectById(userVipCard.getCardTypeId());
            if (vipCardType != null) {
                response.setCardName(vipCardType.getName());
                response.setLevel(vipCardType.getType());
            } else {
                response.setCardName("");
                response.setLevel(0);
            }
        }
        
        return response;
    }
    
    /**
     * 用户购买VIP
     */
    public PurchaseVipResponse purchaseVip(Long userId, PurchaseVipRequest request) {
        // 参数校验
        if (request.getCardTypeId() == null) {
            throw new RuntimeException("请选择VIP卡类型");
        }
        
        // 查询VIP卡类型是否存在且上架
        VipCardType vipCardType = vipCardTypeMapper.selectById(request.getCardTypeId());
        if (vipCardType == null) {
            throw new RuntimeException("VIP卡类型不存在");
        }
        if (vipCardType.getStatus() != 1) {
            throw new RuntimeException("该VIP卡已下架");
        }
        
        // 检查用户是否有生效中的VIP卡，不允许重复购买
        Long activeVipCount = userVipCardMapper.selectCount(
            new LambdaQueryWrapper<UserVipCard>()
                .eq(UserVipCard::getUserId, userId)
                .eq(UserVipCard::getStatus, 1) // 生效中
                .le(UserVipCard::getStartTime, LocalDateTime.now())
                .ge(UserVipCard::getEndTime, LocalDateTime.now())
        );
        
        if (activeVipCount > 0) {
            throw new RuntimeException("您已有生效中的VIP卡，无法重复购买");
        }
        
        // 计算VIP卡的生效时间和失效时间
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startTime = now;
        LocalDateTime endTime;
        
        // 根据卡类型计算结束时间
        if (vipCardType.getType() == 1) {
            // 次卡：有效期30天
            endTime = now.plusDays(30);
        } else if (vipCardType.getType() == 2) {
            // 月卡：有效期30天
            endTime = now.plusDays(30);
        } else if (vipCardType.getType() == 3) {
            // 年卡：有效期365天
            endTime = now.plusDays(365);
        } else {
            throw new RuntimeException("不支持的VIP卡类型");
        }
        
        // 创建购买记录
        VipPurchaseRecord purchaseRecord = new VipPurchaseRecord();
        purchaseRecord.setUserId(userId);
        purchaseRecord.setCardTypeId(request.getCardTypeId());
        purchaseRecord.setAmount(vipCardType.getPrice());
        purchaseRecord.setPayTime(now);
        purchaseRecord.setCreateTime(now);
        purchaseRecord.setUpdateTime(now);
        
        vipPurchaseRecordMapper.insert(purchaseRecord);
        
        // 创建用户VIP卡记录
        UserVipCard userVipCard = new UserVipCard();
        userVipCard.setUserId(userId);
        userVipCard.setCardTypeId(request.getCardTypeId());
        userVipCard.setStartTime(startTime);
        userVipCard.setEndTime(endTime);
        userVipCard.setStatus(1); // 生效中
        userVipCard.setCreateTime(now);
        userVipCard.setUpdateTime(now);
        
        // 如果是次卡，设置剩余次数
        if (vipCardType.getType() == 1) {
            userVipCard.setRemainingCount(vipCardType.getUsageCount());
        } else {
            userVipCard.setRemainingCount(null);
        }
        
        userVipCardMapper.insert(userVipCard);
        
        // 返回订单号（购买记录ID）
        return new PurchaseVipResponse(purchaseRecord.getId());
    }
}
