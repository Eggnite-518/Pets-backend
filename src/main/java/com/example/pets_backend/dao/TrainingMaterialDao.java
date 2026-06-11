package com.example.pets_backend.dao;

import com.example.pets_backend.dao.entity.TrainingMaterialDO;
import com.example.pets_backend.dao.mapper.TrainingMaterialMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TrainingMaterialDao {

    private final TrainingMaterialMapper mapper;

    public TrainingMaterialDO selectLatest() {
        return mapper.selectLatest();
    }

    public List<TrainingMaterialDO> selectRequiredCurriculum() {
        return mapper.selectRequiredCurriculum();
    }

    public TrainingMaterialDO selectByMaterialId(Long materialId) {
        return mapper.selectByMaterialId(materialId);
    }
}
