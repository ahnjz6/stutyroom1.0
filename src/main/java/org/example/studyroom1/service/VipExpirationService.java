package org.example.studyroom1.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.studyroom1.entity.Message;
import org.example.studyroom1.entity.User;
import org.example.studyroom1.entity.UserVipCard;
import org.example.studyroom1.entity.VipCardType;
import org.example.studyroom1.mapper.MessageMapper;
import org.example.studyroom1.mapper.UserMapper;
import org.example.studyroom1.mapper.UserVipCardMapper;
import org.example.studyroom1.mapper.VipCardTypeMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * VIP过期检测定时任务服务
 */
@Service
@RequiredArgsConstructor
public class VipExpirationService {
    
    private final UserVipCardMapper userVipCardMapper;
    private final UserMapper userMapper;
    private final MessageMapper messageMapper;
    private final VipCardTypeMapper vipCardTypeMapper;
    
    /**
     * 服务启动后自动执行首次检测
     * 延迟1秒后执行，只执行一次
     */
    @Scheduled(initialDelay = 1000, fixedDelay = Long.MAX_VALUE)
    public void init() {
        System.out.println("=== VIP过期检测服务启动，执行首次检测 ===");
        try {
            checkAndExpireVipCards();
            checkExpiringVipCards();
            System.out.println("=== 首次检测完成 ===");
        } catch (Exception e) {
            System.err.println("首次检测失败: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 每隔30秒执行一次VIP卡过期检测
     * cron表达式: 0/30 * * * * ? 表示每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    @Transactional
    public void checkAndExpireVipCards() {
        LocalDateTime now = LocalDateTime.now();
        
        // 查询所有生效中但已过期的VIP卡
        List<UserVipCard> expiredVipCards = userVipCardMapper.selectList(
            new LambdaQueryWrapper<UserVipCard>()
                .eq(UserVipCard::getStatus, 1) // 状态为生效中
                .lt(UserVipCard::getEndTime, now) // 结束时间小于当前时间（已过期）
        );
        
        if (expiredVipCards.isEmpty()) {
            return;
        }
        
        System.out.println("检测到 " + expiredVipCards.size() + " 张VIP卡已过期，开始处理...");
        
        // 处理每张过期的VIP卡
        for (UserVipCard vipCard : expiredVipCards) {
            try {
                // 1. 更新VIP卡状态为已过期（status=0）
                vipCard.setStatus(0);
                vipCard.setUpdateTime(now);
                userVipCardMapper.updateById(vipCard);
                
                // 2. 检查该用户是否还有其他生效中的VIP卡
                Long activeVipCount = userVipCardMapper.selectCount(
                    new LambdaQueryWrapper<UserVipCard>()
                        .eq(UserVipCard::getUserId, vipCard.getUserId())
                        .eq(UserVipCard::getStatus, 1) // 生效中
                        .le(UserVipCard::getStartTime, now)
                        .ge(UserVipCard::getEndTime, now)
                );
                
                // 3. 如果用户没有其他生效中的VIP卡，则将用户的isVip设置为false
                if (activeVipCount == 0) {
                    User user = userMapper.selectById(vipCard.getUserId());
                    if (user != null && user.isVip()) {
                        user.setVip(false);
                        user.setUpdateTime(now);
                        userMapper.updateById(user);
                        System.out.println("用户 " + user.getUsername() + " 的VIP已过期，isVip设置为false");
                    }
                }
                
                System.out.println("VIP卡 ID:" + vipCard.getId() + " 已标记为过期");
                
            } catch (Exception e) {
                System.err.println("处理VIP卡 ID:" + vipCard.getId() + " 时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("VIP卡过期检测完成");
    }
    
    /**
     * 每隔30秒执行一次VIP即将过期检测
     * cron表达式: 0/30 * * * * ? 表示每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    @Transactional
    public void checkExpiringVipCards() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneDayLater = now.plusDays(1);
        
        System.out.println("开始检测即将过期的VIP卡...");
        
        // 1. 检测时间型VIP卡（月卡、年卡）：距离过期还有1天以内
        List<UserVipCard> expiringTimeCards = userVipCardMapper.selectList(
            new LambdaQueryWrapper<UserVipCard>()
                .eq(UserVipCard::getStatus, 1) // 生效中
                .gt(UserVipCard::getEndTime, now) // 未过期
                .le(UserVipCard::getEndTime, oneDayLater) // 24小时内过期
        );
        
        for (UserVipCard vipCard : expiringTimeCards) {
            try {
                // 查询VIP卡类型
                VipCardType cardType = vipCardTypeMapper.selectById(vipCard.getCardTypeId());
                if (cardType != null && (cardType.getType() == 2 || cardType.getType() == 3)) {
                    // 只处理月卡和年卡
                    sendExpirationNotification(vipCard.getUserId(), cardType.getName(), vipCard.getEndTime(), null);
                }
            } catch (Exception e) {
                System.err.println("处理即将过期的时间型VIP卡时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // 2. 检测次卡：剩余次数为1次
        List<UserVipCard> expiringCountCards = userVipCardMapper.selectList(
            new LambdaQueryWrapper<UserVipCard>()
                .eq(UserVipCard::getStatus, 1) // 生效中
                .eq(UserVipCard::getRemainingCount, 1) // 剩余1次
        );
        
        for (UserVipCard vipCard : expiringCountCards) {
            try {
                // 查询VIP卡类型
                VipCardType cardType = vipCardTypeMapper.selectById(vipCard.getCardTypeId());
                if (cardType != null && cardType.getType() == 1) {
                    // 只处理次卡
                    sendExpirationNotification(vipCard.getUserId(), cardType.getName(), vipCard.getEndTime(), 1);
                }
            } catch (Exception e) {
                System.err.println("处理即将用完的次卡时发生错误: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        System.out.println("VIP即将过期检测完成");
    }
    
    /**
     * 发送VIP即将过期通知
     * 
     * @param userId 用户ID
     * @param cardName VIP卡名称
     * @param expireTime 过期时间
     * @param remainingCount 剩余次数（仅次卡）
     */
    private void sendExpirationNotification(Long userId, String cardName, LocalDateTime expireTime, Integer remainingCount) {
        // 获取今天的起始时间（00:00:00）
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        
        String title;
        if (remainingCount != null) {
            title = "VIP次卡即将用完";
        } else {
            title = "VIP会员即将过期";
        }
        
        // 检查今天是否已发送过相同类型的通知
        Long todayNotificationCount = messageMapper.selectCount(
            new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
                .eq(Message::getTitle, title)
                .ge(Message::getCreateTime, todayStart)
        );
        
        if (todayNotificationCount > 0) {
            System.out.println("用户 " + userId + " 今天已发送过【" + title + "】通知，跳过");
            return;
        }
        
        String content;
        if (remainingCount != null) {
            // 次卡剩余1次
            content = String.format("您的%s仅剩%d次使用机会，请及时充值或购买新卡。", cardName, remainingCount);
        } else {
            // 时间型VIP卡即将过期
            content = String.format("您的%s将于%s到期，请及时续费以保持VIP权益。", 
                cardName, 
                expireTime.toString()
            );
        }
        
        // 创建消息通知
        Message message = new Message();
        message.setUserId(userId);
        message.setTitle(title);
        message.setContent(content);
        message.setMessageType(2); // 2表示VIP相关通知
        message.setIsRead(0); // 未读
        message.setCreateTime(LocalDateTime.now());
        
        messageMapper.insert(message);
        System.out.println("✓ 已发送通知给用户ID: " + userId + ", 标题: " + title);
    }
}
