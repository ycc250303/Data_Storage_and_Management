package com.query.mysql.mapper;

import com.query.mysql.entity.MovieDirectors;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
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
