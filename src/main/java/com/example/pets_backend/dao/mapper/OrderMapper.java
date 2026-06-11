package com.example.pets_backend.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.pets_backend.dao.entity.OrderDO;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper extends BaseMapper<OrderDO> {

    IPage<OrderDO> selectOpenOrderPageByFilters(IPage<OrderDO> page,
                                                @Param("petType") Integer petType,
                                                @Param("serviceType") Integer serviceType);

    int updateProviderIdAndStatus(@Param("orderId") Long orderId,
                                  @Param("providerId") Long providerId,
                                  @Param("status") Integer status);

    int updateStatus(@Param("orderId") Long orderId,
                     @Param("status") Integer status);

    List<Long> selectInactiveProviders(@Param("cutoff") LocalDateTime cutoff);
}
