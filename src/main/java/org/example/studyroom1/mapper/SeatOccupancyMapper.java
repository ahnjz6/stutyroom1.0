package org.example.studyroom1.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.example.studyroom1.entity.SeatOccupancy;

/**
 * 座位时段占用Mapper接口
 */
@Mapper
public interface SeatOccupancyMapper extends BaseMapper<SeatOccupancy> {
}
