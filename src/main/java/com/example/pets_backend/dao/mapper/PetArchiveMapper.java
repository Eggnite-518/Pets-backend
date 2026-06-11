package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.PetArchiveDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PetArchiveMapper extends BaseMapper<PetArchiveDO> {

    PetArchiveDO selectByPetIdAndOwnerId(@Param("petId") Long petId, @Param("ownerId") Long ownerId);

    List<PetArchiveDO> selectListByOwnerId(@Param("ownerId") Long ownerId,
                                           @Param("petName") String petName,
                                           @Param("petType") Integer petType);

    int updateByPetIdAndOwnerId(PetArchiveDO petArchive);

    int deleteByPetIdAndOwnerId(@Param("petId") Long petId, @Param("ownerId") Long ownerId);
}



