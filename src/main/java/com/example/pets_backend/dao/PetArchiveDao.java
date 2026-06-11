package com.example.pets_backend.dao;

import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dao.mapper.PetArchiveMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PetArchiveDao {

    private final PetArchiveMapper petArchiveMapper;

    public int insert(PetArchiveDO petArchive) {
        return petArchiveMapper.insert(petArchive);
    }

    public PetArchiveDO selectByPetIdAndOwnerId(Long petId, Long ownerId) {
        return petArchiveMapper.selectByPetIdAndOwnerId(petId, ownerId);
    }

    public List<PetArchiveDO> selectListByOwnerId(Long ownerId, String petName, Integer petType) {
        return petArchiveMapper.selectListByOwnerId(ownerId, petName, petType);
    }

    public int updateByPetIdAndOwnerId(PetArchiveDO petArchive) {
        return petArchiveMapper.updateByPetIdAndOwnerId(petArchive);
    }

    public int deleteByPetIdAndOwnerId(Long petId, Long ownerId) {
        return petArchiveMapper.deleteByPetIdAndOwnerId(petId, ownerId);
    }

    public List<PetArchiveDO> selectByIds(List<Long> petIds) {
        if (petIds == null || petIds.isEmpty()) {
            return List.of();
        }
        return petArchiveMapper.selectBatchIds(petIds);
    }
}

