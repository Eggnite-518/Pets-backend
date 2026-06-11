package com.example.pets_backend.dao;

import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dao.mapper.UserMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserDao {

    private final UserMapper userMapper;

    public UserDO selectById(Long userId) {
        return userMapper.selectById(userId);
    }

    public UserDO selectByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    public void insert(UserDO user) {
        userMapper.insert(user);
    }

    public void updateById(UserDO user) {
        userMapper.updateById(user);
    }

    public List<UserDO> selectByIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return List.of();
        }
        return userMapper.selectBatchIds(userIds);
    }
}
