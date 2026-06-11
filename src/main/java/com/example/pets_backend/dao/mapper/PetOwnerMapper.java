package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.PetOwnerDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PetOwnerMapper extends BaseMapper<PetOwnerDO> {

    PetOwnerDO selectByOwnerId(@Param("ownerId") Long ownerId);

    int updateByOwnerId(PetOwnerDO petOwner);

    int deleteByOwnerId(@Param("ownerId") Long ownerId);
}

