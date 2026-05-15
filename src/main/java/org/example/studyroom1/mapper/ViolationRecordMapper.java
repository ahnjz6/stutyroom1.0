package org.example.studyroom1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.studyroom1.entity.ViolationRecord;

/**
 * 违约记录Mapper接口
 */
@Mapper
public interface ViolationRecordMapper extends BaseMapper<ViolationRecord> {
}
