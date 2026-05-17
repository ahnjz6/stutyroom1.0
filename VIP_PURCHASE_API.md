# VIP购买接口文档

## 接口信息

**接口地址**: `/user/vip/purchase`

**请求方法**: `POST`

**需要认证**: 是（需要在Header中携带Authorization Token）

## 请求参数

### Headers
```
Authorization: Bearer {token}
Content-Type: application/json
```

### Request Body
```json
{
    "cardTypeId": 1
}
```

**参数说明**:
- `cardTypeId`: VIP卡类型ID（必填）
  - 1: 次卡
  - 2: 月卡
  - 3: 年卡

## 响应结果

### 成功响应
```json
{
    "code": 200,
    "data": {
        "orderId": 123456
    },
    "msg": "购买成功"
}
```

**响应字段说明**:
- `code`: 响应码，200表示成功
- `data.orderId`: 订单号（购买记录的主键ID）
- `msg`: 响应消息

### 失败响应

#### 1. 未选择VIP卡类型
```json
{
    "code": 500,
    "data": null,
    "msg": "请选择VIP卡类型"
}
```

#### 2. VIP卡类型不存在
```json
{
    "code": 500,
    "data": null,
    "msg": "VIP卡类型不存在"
}
```

#### 3. VIP卡已下架
```json
{
    "code": 500,
    "data": null,
    "msg": "该VIP卡已下架"
}
```

#### 4. 已有生效中的VIP卡
```json
{
    "code": 500,
    "data": null,
    "msg": "您已有生效中的VIP卡，无法重复购买"
}
```

#### 5. 无效的Token
```json
{
    "code": 500,
    "data": null,
    "msg": "无效的Token"
}
```

## 业务逻辑说明

### 1. 重复购买限制
- 系统会检查用户是否有**生效中**的VIP卡
- 如果存在生效中的VIP卡（status=1 且当前时间在startTime和endTime之间），则不允许购买
- 已过期的VIP卡不影响新购买

### 2. VIP卡有效期计算
- **次卡（type=1）**: 有效期30天，包含指定的使用次数
- **月卡（type=2）**: 有效期30天
- **年卡（type=3）**: 有效期365天

### 3. 数据写入流程
1. 验证VIP卡类型是否存在且上架
2. 检查用户是否有生效中的VIP卡（防止重复购买）
3. 计算VIP卡的生效时间和失效时间
4. 在 `vip_purchase_record` 表中创建购买记录
5. 在 `user_vip_card` 表中创建用户VIP卡记录
6. 返回购买记录的ID作为订单号

### 4. 数据库表更新

#### vip_purchase_record（购买记录表）
- `user_id`: 用户ID
- `card_type_id`: VIP卡类型ID
- `pay_time`: 支付时间
- `create_time`: 创建时间
- `update_time`: 更新时间

#### user_vip_card（用户VIP卡表）
- `user_id`: 用户ID
- `card_type_id`: VIP卡类型ID
- `remaining_count`: 剩余次数（仅次卡有值）
- `start_time`: 生效时间
- `end_time`: 失效时间
- `status`: 状态（1=生效中）
- `create_time`: 创建时间
- `update_time`: 更新时间

## 使用示例

### cURL示例
```bash
curl -X POST http://localhost:8080/user/vip/purchase \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{"cardTypeId": 2}'
```

### JavaScript示例
```javascript
fetch('http://localhost:8080/user/vip/purchase', {
    method: 'POST',
    headers: {
        'Authorization': 'Bearer ' + token,
        'Content-Type': 'application/json'
    },
    body: JSON.stringify({
        cardTypeId: 2
    })
})
.then(response => response.json())
.then(data => {
    console.log('订单号:', data.data.orderId);
})
.catch(error => {
    console.error('购买失败:', error);
});
```

## 注意事项

1. **必须先登录**: 调用此接口前需要先通过 `/user/login` 接口获取token
2. **不能重复购买**: 用户只能同时拥有一个生效中的VIP卡
3. **VIP卡类型必须上架**: 只能购买status=1（上架）的VIP卡类型
4. **订单号唯一**: 返回的orderId是购买记录的主键ID，可用于后续查询
