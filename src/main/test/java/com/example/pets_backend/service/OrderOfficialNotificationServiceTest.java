package com.example.pets_backend.service;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.service.official.OfficialMessageService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderOfficialNotificationServiceTest {

    @Mock
    private OfficialMessageService officialMessageService;

    @Mock
    private UserDao userDao;

    @InjectMocks
    private OrderOfficialNotificationService orderOfficialNotificationService;

    @Test
    void notifyOwnerOnProviderApplicationSendsOfficialMessage() {
        OrderDO order = buildOrder(2002L, 1001L);
        UserDO provider = new UserDO();
        provider.setNickname("阿周");
        when(userDao.selectById(1002L)).thenReturn(provider);

        orderOfficialNotificationService.notifyOwnerOnProviderApplication(order, 1002L);

        verify(officialMessageService).sendSystemOfficialMessage(
                eq(2002L),
                eq(1001L),
                eq("【报名通知】宠托师阿周已报名您的订单 #2002，请前往订单详情查看候选人并录用。"));
    }

    @Test
    void notifyCaretakerOnSelectedSendsOfficialMessage() {
        OrderDO order = buildOrder(2002L, 1001L);

        orderOfficialNotificationService.notifyCaretakerOnSelected(order, 1002L);

        verify(officialMessageService).sendSystemOfficialMessage(
                eq(2002L),
                eq(1002L),
                eq("【录用通知】恭喜！您已被宠主录用为订单 #2002 的服务宠托师，服务日期 2026-05-03。请等待宠主完成付款后按约定时间履约。"));
    }

    private OrderDO buildOrder(Long orderId, Long ownerId) {
        OrderDO order = new OrderDO();
        order.setOrderId(orderId);
        order.setOwnerId(ownerId);
        order.setTotalAmount(BigDecimal.valueOf(98));
        order.setServiceDate(LocalDate.of(2026, 5, 3));
        return order;
    }
}
