package com.query.hive.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.query.hive.entity.MovieDirectors;
import org.apache.ibatis.annotations.Mapper;

/**
 * <p>
 * 电影-导演关联表 Mapper 接口
 * </p>
 *
 * @author Data Warehouse Team
 * @since 2025-12-13
 */
@Mapper
public interface MovieDirectorsMapper extends BaseMapper<MovieDirectors> {

}
