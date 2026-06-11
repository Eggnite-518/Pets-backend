package com.example.pets_backend.dao;

import com.example.pets_backend.dao.entity.WithdrawalRecordDO;
import com.example.pets_backend.dao.mapper.WithdrawalRecordMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class WithdrawalRecordDao {

    private final WithdrawalRecordMapper withdrawalRecordMapper;

    public void insert(WithdrawalRecordDO withdrawalRecord) {
        withdrawalRecordMapper.insert(withdrawalRecord);
    }
}
