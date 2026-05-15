package org.example.studyroom1.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import org.example.studyroom1.entity.User;
import org.example.studyroom1.mapper.UserMapper;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * 数据初始化（仅用于测试）
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    
    private final UserMapper userMapper;
    
    @Override
    public void run(String... args) {
        // 检查是否已有测试用户
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, "testuser")
        );
        
        if (count == 0) {
            // 创建测试用户（明文密码）
            User user = new User();
            user.setUsername("testuser");
            user.setPassword("123456"); // 明文密码：123456
            user.setPhone("13800138000");
            user.setStatus(1);
            user.setLoginFailCount(0);
            user.setViolationCount(0);
            
            userMapper.insert(user);
            System.out.println("测试用户创建成功！用户名：testuser，密码：123456");
        }
    }
}
