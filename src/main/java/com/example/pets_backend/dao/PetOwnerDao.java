package com.example.pets_backend.dao;

import com.example.pets_backend.dao.entity.PetOwnerDO;
import com.example.pets_backend.dao.mapper.PetOwnerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PetOwnerDao {

    private final PetOwnerMapper petOwnerMapper;

    public int insert(PetOwnerDO petOwner) {
        return petOwnerMapper.insert(petOwner);
    }

    public PetOwnerDO selectByOwnerId(Long ownerId) {
        return petOwnerMapper.selectByOwnerId(ownerId);
    }

    public int updateByOwnerId(PetOwnerDO petOwner) {
        return petOwnerMapper.updateByOwnerId(petOwner);
    }

    public int deleteByOwnerId(Long ownerId) {
        return petOwnerMapper.deleteByOwnerId(ownerId);
    }
}

