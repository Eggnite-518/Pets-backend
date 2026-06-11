package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.TrainingMaterialDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TrainingMaterialMapper extends BaseMapper<TrainingMaterialDO> {

    @Select("""
            SELECT *
            FROM training_materials
            WHERE deleted = 0
            ORDER BY material_id DESC
            LIMIT 1
            """)
    TrainingMaterialDO selectLatest();

    @Select("""
            SELECT *
            FROM training_materials
            WHERE deleted = 0
              AND is_required = 1
            ORDER BY sort_order ASC, material_id ASC
            """)
    List<TrainingMaterialDO> selectRequiredCurriculum();

    @Select("""
            SELECT *
            FROM training_materials
            WHERE deleted = 0
              AND material_id = #{materialId}
            LIMIT 1
            """)
    TrainingMaterialDO selectByMaterialId(Long materialId);
}
