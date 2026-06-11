package com.example.pets_backend.dao;

import com.example.pets_backend.dao.entity.UserCreditProfileDO;
import com.example.pets_backend.dao.mapper.UserCreditProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCreditProfileDao {

    private final UserCreditProfileMapper mapper;

    public UserCreditProfileDO selectById(Long userId) {
        return mapper.selectById(userId);
    }

    public void insert(UserCreditProfileDO profile) {
        mapper.insert(profile);
    }

    public void updateById(UserCreditProfileDO profile) {
        mapper.updateById(profile);
    }
}

