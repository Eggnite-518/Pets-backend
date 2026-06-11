package com.example.pets_backend.dao;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.pets_backend.dao.entity.SmsVerificationCodeDO;
import com.example.pets_backend.dao.mapper.SmsVerificationCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SmsVerificationCodeDao {

    private final SmsVerificationCodeMapper mapper;

    public void insert(SmsVerificationCodeDO code) {
        mapper.insert(code);
    }

    public SmsVerificationCodeDO selectLatestValid(String phone) {
        return mapper.selectLatestValid(phone);
    }

    public int markUsed(Long id) {
        return mapper.markUsed(id);
    }

    public boolean existsByPhoneWithinSeconds(String phone, int seconds) {
        LambdaQueryWrapper<SmsVerificationCodeDO> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SmsVerificationCodeDO::getPhone, phone)
                .apply("created_at > DATE_SUB(NOW(), INTERVAL {0} SECOND)", seconds)
                .last("LIMIT 1");
        return mapper.selectOne(wrapper) != null;
    }
}
