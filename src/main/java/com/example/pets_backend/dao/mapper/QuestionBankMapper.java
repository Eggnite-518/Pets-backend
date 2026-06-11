package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.QuestionBankDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface QuestionBankMapper extends BaseMapper<QuestionBankDO> {

    @Select("""
            SELECT *
            FROM question_bank
            WHERE deleted = 0
              AND question_type = #{type}
            ORDER BY RAND()
            LIMIT #{limit}
            """)
    List<QuestionBankDO> selectRandomByType(@Param("type") int type, @Param("limit") int limit);
}

