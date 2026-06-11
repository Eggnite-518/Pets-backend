package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.ReviewDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ReviewMapper extends BaseMapper<ReviewDO> {
}
