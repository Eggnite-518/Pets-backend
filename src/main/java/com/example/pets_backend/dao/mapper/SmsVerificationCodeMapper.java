package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.SmsVerificationCodeDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmsVerificationCodeMapper extends BaseMapper<SmsVerificationCodeDO> {

    SmsVerificationCodeDO selectLatestValid(@Param("phone") String phone);

    int markUsed(@Param("id") Long id);
}
