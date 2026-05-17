# VIP自动过期检测与通知功能说明

## 功能概述

系统实现了两个定时任务，自动管理VIP卡的过期状态和发送即将过期通知。

## 定时任务详情

### 1. VIP卡过期检测任务

**执行频率**: 每30秒执行一次  
**方法**: `checkAndExpireVipCards()`  
**Cron表达式**: `0/30 * * * * ?`

#### 功能逻辑

1. **检测已过期的VIP卡**
   - 查询条件：`status = 1`（生效中）且 `endTime < 当前时间`
   
2. **更新VIP卡状态**
   - 将过期的VIP卡 `status` 从 1 改为 0（已过期）
   - 更新 `updateTime` 字段

3. **更新用户VIP状态**
   - 检查该用户是否还有其他生效中的VIP卡
   - 如果没有其他生效VIP卡，将用户的 `isVip` 设置为 `false`

4. **异常处理**
   - 单个VIP卡处理失败不影响其他卡的处理
   - 详细的控制台日志输出

---

### 2. VIP即将过期通知任务

**执行频率**: 每小时整点执行一次  
**方法**: `checkExpiringVipCards()`  
**Cron表达式**: `0 0 * * * ?`

#### 功能逻辑

##### A. 时间型VIP卡通知（月卡、年卡）

**触发条件**: 
- VIP卡状态为生效中（status=1）
- 距离过期时间还有24小时以内
- VIP卡类型为月卡（type=2）或年卡（type=3）

**通知内容示例**:
```
标题: VIP会员即将过期
内容: 您的月卡将于2026-05-18T14:15:22到期，请及时续费以保持VIP权益。
```

##### B. 次卡剩余次数通知

**触发条件**:
- VIP卡状态为生效中（status=1）
- 剩余次数为1次（remainingCount=1）
- VIP卡类型为次卡（type=1）

**通知内容示例**:
```
标题: VIP次卡即将用完
内容: 您的次卡仅剩1次使用机会，请及时充值或购买新卡。
```

#### 通知方式

- 通过站内消息系统发送通知
- 消息类型：`messageType = 2`（VIP相关通知）
- 初始状态：未读（isRead=0）
- 用户可通过 `/user/message/list` 接口查看通知

---

## 技术实现

### 核心类

- **VipExpirationService**: VIP过期检测服务
- **@EnableScheduling**: 启用Spring定时任务支持
- **@Scheduled**: 定义定时任务执行规则
- **@Transactional**: 保证数据一致性

### 数据库操作

1. **查询过期VIP卡**
   ```java
   userVipCardMapper.selectList(
       new LambdaQueryWrapper<UserVipCard>()
           .eq(UserVipCard::getStatus, 1)
           .lt(UserVipCard::getEndTime, now)
   )
   ```

2. **更新VIP卡状态**
   ```java
   vipCard.setStatus(0);
   userVipCardMapper.updateById(vipCard);
   ```

3. **更新用户VIP状态**
   ```java
   user.setVip(false);
   userMapper.updateById(user);
   ```

4. **创建通知消息**
   ```java
   Message message = new Message();
   message.setUserId(userId);
   message.setTitle(title);
   message.setContent(content);
   message.setMessageType(2);
   messageMapper.insert(message);
   ```

---

## 配置说明

### 调整执行频率

如果需要调整定时任务的执行频率，可以修改 `@Scheduled` 注解中的 cron 表达式：

| Cron表达式 | 含义 |
|-----------|------|
| `0/30 * * * * ?` | 每30秒执行 |
| `0 * * * * ?` | 每分钟执行 |
| `0 0 * * * ?` | 每小时执行 |
| `0 0 0 * * ?` | 每天凌晨执行 |
| `0 0 */6 * * ?` | 每6小时执行 |

### Cron表达式格式

```
秒 分 时 日 月 周
```

示例：
- `0/30 * * * * ?` = 每30秒
- `0 0 9 * * ?` = 每天早上9点
- `0 0 9,18 * * ?` = 每天9点和18点

---

## 注意事项

### 1. 重复通知问题

当前实现可能会在每次检测时重复发送通知。如果需要避免重复通知，建议：

**方案A**: 添加通知记录表
```sql
CREATE TABLE vip_notification_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    vip_card_id BIGINT NOT NULL,
    notification_type VARCHAR(50), -- 'EXPIRING' 或 'LOW_COUNT'
    create_time DATETIME
);
```

**方案B**: 在消息表中添加唯一标识
- 发送前检查是否已存在相同内容的未读消息

### 2. 性能优化

如果用户量很大，可以考虑：

1. **增加检测间隔**: 从30秒改为5分钟或更长
2. **分批处理**: 每次只处理一部分过期VIP卡
3. **异步处理**: 使用 `@Async` 异步发送通知
4. **索引优化**: 确保 `user_vip_card` 表的 `status`、`end_time` 字段有索引

### 3. 日志记录

当前使用 `System.out.println` 输出日志，生产环境建议：

- 使用 SLF4J + Logback
- 记录到日志文件
- 重要的状态变更写入数据库日志表

---

## 测试建议

### 1. 测试VIP过期检测

```sql
-- 创建一个即将过期的VIP卡（5分钟后过期）
INSERT INTO user_vip_card (user_id, card_type_id, remaining_count, start_time, end_time, status, create_time, update_time)
VALUES (1, 2, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 5 MINUTE), 1, NOW(), NOW());

-- 等待30秒后检查
SELECT * FROM user_vip_card WHERE user_id = 1;
SELECT is_vip FROM user WHERE id = 1;
```

### 2. 测试即将过期通知

```sql
-- 创建一个24小时内过期的VIP卡
INSERT INTO user_vip_card (user_id, card_type_id, remaining_count, start_time, end_time, status, create_time, update_time)
VALUES (2, 2, NULL, NOW(), DATE_ADD(NOW(), INTERVAL 12 HOUR), 1, NOW(), NOW());

-- 等待1小时后检查消息表
SELECT * FROM message WHERE user_id = 2 ORDER BY create_time DESC LIMIT 1;
```

### 3. 测试次卡剩余1次通知

```sql
-- 创建一个剩余1次的次卡
INSERT INTO user_vip_card (user_id, card_type_id, remaining_count, start_time, end_time, status, create_time, update_time)
VALUES (3, 1, 1, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1, NOW(), NOW());

-- 等待1小时后检查消息表
SELECT * FROM message WHERE user_id = 3 ORDER BY create_time DESC LIMIT 1;
```

---

## 相关文件

- **主应用类**: `Studyroom1Application.java` - 添加了 `@EnableScheduling`
- **定时任务服务**: `VipExpirationService.java` - 实现过期检测和通知逻辑
- **实体类**: 
  - `UserVipCard.java` - 用户VIP卡实体
  - `User.java` - 用户实体
  - `Message.java` - 消息实体
- **Mapper接口**:
  - `UserVipCardMapper.java`
  - `UserMapper.java`
  - `MessageMapper.java`
  - `VipCardTypeMapper.java`

---

## 总结

✅ **已实现功能**:
1. 每30秒自动检测并更新过期VIP卡状态
2. 自动更新用户的 `isVip` 字段
3. 每小时检测即将过期的VIP卡（24小时内）
4. 检测次卡剩余1次的情况
5. 通过站内消息发送通知

⚠️ **待优化项**:
1. 防止重复发送通知
2. 添加更详细的日志记录
3. 考虑性能优化（大批量数据处理）
4. 可以添加邮件、短信等其他通知方式
