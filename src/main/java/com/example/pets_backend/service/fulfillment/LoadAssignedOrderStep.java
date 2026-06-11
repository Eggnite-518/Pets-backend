package com.example.pets_backend.service.fulfillment;

import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LoadAssignedOrderStep implements FulfillmentNodeStep {

    private final OrderDao orderDao;

    @Override
    public FulfillmentStepKey key() {
        return FulfillmentStepKey.LOAD_ASSIGNED_ORDER;
    }

    @Override
    public void handle(FulfillmentContext context) {
        Long providerId = currentUserId();
        OrderDO order = orderDao.selectById(context.orderId());
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }
        if (!providerId.equals(order.getProviderId())) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OWNER_ERROR);
        }
        context.order(order);
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }
}
