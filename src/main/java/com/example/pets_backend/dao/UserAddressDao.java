package com.example.pets_backend.dao;

import com.example.pets_backend.dao.entity.UserAddressDO;
import com.example.pets_backend.dao.mapper.UserAddressMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserAddressDao {

    private final UserAddressMapper userAddressMapper;

    public int insert(UserAddressDO userAddress) {
        return userAddressMapper.insert(userAddress);
    }

    public UserAddressDO selectByAddressIdAndUserId(Long addressId, Long userId) {
        return userAddressMapper.selectByAddressIdAndUserId(addressId, userId);
    }

    public List<UserAddressDO> selectListByUserId(Long userId) {
        return userAddressMapper.selectListByUserId(userId);
    }

    public int updateByAddressIdAndUserId(UserAddressDO userAddress) {
        return userAddressMapper.updateByAddressIdAndUserId(userAddress);
    }

    public int deleteByAddressIdAndUserId(Long addressId, Long userId) {
        return userAddressMapper.deleteByAddressIdAndUserId(addressId, userId);
    }

    public int resetDefaultByUserId(Long userId) {
        return userAddressMapper.resetDefaultByUserId(userId);
    }

    public int updateFamilySopByAddressIdAndUserId(UserAddressDO userAddress) {
        return userAddressMapper.updateFamilySopByAddressIdAndUserId(userAddress);
    }
}
