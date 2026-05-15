# 用户登录接口说明

## 接口信息

**接口地址**: `POST /user/login`

**请求头**: 
- Content-Type: application/json

## 请求参数

```json
{
    "username": "testuser",
    "password": "123456"
}
```

## 响应结果

### 成功响应

```json
{
    "code": 0,
    "msg": "登录成功",
    "data": {
        "id": 1,
        "username": "testuser",
        "token": "eyJhbGciOiJIUzI1NiJ9...",
        "status": 1,
        "isVip": 0
    }
}
```

### 失败响应

```json
{
    "code": 1,
    "msg": "用户名或密码错误",
    "data": null
}
```

## 字段说明

### 请求参数
- `username`: 用户名（必填）
- `password`: 密码（必填）

### 响应数据
- `code`: 响应码，0表示成功，1表示失败
- `msg`: 响应消息
- `data`: 响应数据
  - `id`: 用户ID
  - `username`: 用户名
  - `token`: JWT令牌（用于后续请求的身份验证）
  - `status`: 用户状态，0封禁，1正常
  - `isVip`: 是否为VIP，0非VIP，1是VIP

## 功能特性

1. **JWT令牌认证**: 使用JWT生成token，有效期7天
2. **明文密码验证**: 直接使用明文密码进行比对（仅用于开发测试）
3. **登录失败处理**: 
   - 连续失败5次锁定账号30分钟
   - 记录登录日志
4. **VIP状态检测**: 自动检测用户是否有生效的VIP卡
5. **账号状态检查**: 检查账号是否被封禁或锁定

## 测试步骤

1. 启动应用后，会自动创建测试用户：
   - 用户名：`testuser`
   - 密码：`123456`

2. 使用Postman或其他工具发送请求：
   ```
   POST http://localhost:8084/user/login
   Content-Type: application/json
   
   {
       "username": "testuser",
       "password": "123456"
   }
   ```

3. 获取token后，在后续请求的请求头中携带：
   ```
   Authorization: Bearer {token}
   ```

## 技术栈

- Spring Boot 3.5.7
- MyBatis-Plus 3.5.9
- JWT (jjwt 0.12.5)
- MySQL 8.0
- Lombok

## 注意事项

1. JWT密钥目前硬编码在代码中，生产环境应该从配置文件读取
2. 登录IP和User-Agent目前为固定值，实际需要从HttpServletRequest中获取
3. 测试用户仅在数据库为空时创建，生产环境应删除DataInitializer类
4. **重要**：当前使用明文密码验证，仅适用于开发测试环境。生产环境建议使用密码加密
