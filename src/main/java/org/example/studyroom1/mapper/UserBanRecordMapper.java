package org.example.studyroom1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.studyroom1.entity.UserBanRecord;

/**
 * 用户封禁记录Mapper接口
 */
@Mapper
public interface UserBanRecordMapper extends BaseMapper<UserBanRecord> {
}
