package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderApplicationMapper extends BaseMapper<OrderApplicationDO> {

    int countByOrderIdAndProviderId(@Param("orderId") Long orderId, @Param("providerId") Long providerId);

    int deleteByOrderIdAndProviderId(@Param("orderId") Long orderId, @Param("providerId") Long providerId);

    int updateApplyStatusByOrderIdAndProviderId(@Param("orderId") Long orderId,
                                                 @Param("providerId") Long providerId,
                                                 @Param("applyStatus") Integer applyStatus);

    int updateApplyStatusForOthers(@Param("orderId") Long orderId,
                                    @Param("excludeProviderId") Long excludeProviderId,
                                    @Param("applyStatus") Integer applyStatus);
}
