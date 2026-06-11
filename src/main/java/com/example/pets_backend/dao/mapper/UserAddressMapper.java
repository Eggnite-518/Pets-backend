package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.UserAddressDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAddressMapper extends BaseMapper<UserAddressDO> {

    UserAddressDO selectByAddressIdAndUserId(@Param("addressId") Long addressId, @Param("userId") Long userId);

    List<UserAddressDO> selectListByUserId(@Param("userId") Long userId);

    int updateByAddressIdAndUserId(UserAddressDO userAddress);

    int deleteByAddressIdAndUserId(@Param("addressId") Long addressId, @Param("userId") Long userId);

    int resetDefaultByUserId(@Param("userId") Long userId);

    int updateFamilySopByAddressIdAndUserId(UserAddressDO userAddress);
}
