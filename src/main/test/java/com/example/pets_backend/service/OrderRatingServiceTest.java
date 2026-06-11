package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.CreditRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.ReviewDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.CreditRecordDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dto.req.SubmitRatingReqDTO;
import com.example.pets_backend.enums.CreditActionTypeEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.impl.OrderRatingServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderRatingServiceTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private ReviewDao reviewDao;

    @Mock
    private SitterProfileDao sitterProfileDao;

    @Mock
    private CreditRecordDao creditRecordDao;

    private OrderRatingServiceImpl orderRatingService;

    @BeforeEach
    void setUp() {
        orderRatingService = new OrderRatingServiceImpl(orderDao, reviewDao, sitterProfileDao, creditRecordDao);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "小林", 1, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void fiveStarBoostsCreditScoreByFour() {
        when(orderDao.selectById(2001L)).thenReturn(buildCompletedOrder());
        when(reviewDao.existsByOrderIdAndReviewerAndType(2001L, 1001L, 1)).thenReturn(false);
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 80, 0));

        orderRatingService.submitRating(2001L, new SubmitRatingReqDTO(null, 5, "五星好评"));

        ArgumentCaptor<SitterProfileDO> profileCaptor = ArgumentCaptor.forClass(SitterProfileDO.class);
        verify(sitterProfileDao).updateById(profileCaptor.capture());
        assertEquals(84, profileCaptor.getValue().getCreditScore());
        assertEquals(0, profileCaptor.getValue().getIsBanned());

        ArgumentCaptor<CreditRecordDO> recordCaptor = ArgumentCaptor.forClass(CreditRecordDO.class);
        verify(creditRecordDao).insert(recordCaptor.capture());
        assertEquals(4, recordCaptor.getValue().getChangeScore());
        assertEquals(84, recordCaptor.getValue().getScoreAfter());
        assertEquals(CreditActionTypeEnum.FIVE_STAR.getCode(), recordCaptor.getValue().getReasonType());
    }

    @Test
    void lowQualityDeductsFourPoints() {
        when(orderDao.selectById(2001L)).thenReturn(buildCompletedOrder());
        when(reviewDao.existsByOrderIdAndReviewerAndType(2001L, 1001L, 1)).thenReturn(false);
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 80, 0));

        orderRatingService.submitRating(2001L, new SubmitRatingReqDTO(CreditActionTypeEnum.LOW_SERVICE_QUALITY.getCode(), null, "服务质量低"));

        ArgumentCaptor<SitterProfileDO> profileCaptor = ArgumentCaptor.forClass(SitterProfileDO.class);
        verify(sitterProfileDao).updateById(profileCaptor.capture());
        assertEquals(76, profileCaptor.getValue().getCreditScore());
        assertEquals(0, profileCaptor.getValue().getIsBanned());
    }

    @Test
    void seriousAccidentZerosScoreAndBansProvider() {
        when(orderDao.selectById(2001L)).thenReturn(buildCompletedOrder());
        when(reviewDao.existsByOrderIdAndReviewerAndType(2001L, 1001L, 1)).thenReturn(false);
        when(sitterProfileDao.selectById(1002L)).thenReturn(buildProfile(1002L, 92, 0));

        orderRatingService.submitRating(2001L, new SubmitRatingReqDTO(CreditActionTypeEnum.SERIOUS_ACCIDENT.getCode(), null, "严重事故"));

        ArgumentCaptor<SitterProfileDO> profileCaptor = ArgumentCaptor.forClass(SitterProfileDO.class);
        verify(sitterProfileDao).updateById(profileCaptor.capture());
        assertEquals(0, profileCaptor.getValue().getCreditScore());
        assertEquals(1, profileCaptor.getValue().getIsBanned());
        assertEquals(0, profileCaptor.getValue().getVerifyStatus());
    }

    @Test
    void rejectsInvalidActionType() {
        ClientException exception = assertThrows(ClientException.class,
                () -> orderRatingService.submitRating(2001L, new SubmitRatingReqDTO(999, null, "无效")));

        assertEquals(BaseErrorCode.CLIENT_ERROR.code(), exception.getErrorCode());
    }

    private OrderDO buildCompletedOrder() {
        OrderDO order = new OrderDO();
        order.setOrderId(2001L);
        order.setOwnerId(1001L);
        order.setProviderId(1002L);
        order.setStatus(6);
        order.setServiceDate(LocalDate.of(2026, 5, 3));
        order.setTotalAmount(BigDecimal.valueOf(168));
        return order;
    }

    private SitterProfileDO buildProfile(Long providerId, int creditScore, int isBanned) {
        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(providerId);
        profile.setCreditScore(creditScore);
        profile.setIsBanned(isBanned);
        profile.setVerifyStatus(2);
        profile.setDepositAmount(BigDecimal.valueOf(200));
        return profile;
    }
}
