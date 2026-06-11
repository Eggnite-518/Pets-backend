package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

    UserDO selectByPhone(@Param("phone") String phone);
}
