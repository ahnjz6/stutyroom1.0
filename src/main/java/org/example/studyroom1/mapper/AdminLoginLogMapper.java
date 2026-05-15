package org.example.studyroom1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.studyroom1.entity.AdminLoginLog;

/**
 * 管理员登录日志Mapper接口
 */
@Mapper
public interface AdminLoginLogMapper extends BaseMapper<AdminLoginLog> {
}
