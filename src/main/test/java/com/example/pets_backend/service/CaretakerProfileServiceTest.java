package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.ReviewDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SitterTrainingRecordDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.SitterTrainingRecordDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.CaretakerProfileUpdateReqDTO;
import com.example.pets_backend.dto.resp.CaretakerProfileRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CaretakerProfileServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private OrderDao orderDao;

    @Mock
    private ReviewDao reviewDao;

    @Mock
    private SitterProfileDao sitterProfileDao;

    @Mock
    private SitterTrainingRecordDao sitterTrainingRecordDao;

    private CaretakerProfileService caretakerProfileService;

    @BeforeEach
    void setUp() {
        caretakerProfileService = new CaretakerProfileService(userDao, orderDao, reviewDao, sitterProfileDao,
                sitterTrainingRecordDao);
        UserContext.setUser(new UserInfoDTO(1002L, "13800000002", "provider", 2, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void updateProfileIgnoresManualCertLabelsAndReturnsSystemLabels() {
        UserDO user = new UserDO();
        user.setUserId(1002L);
        user.setNickname("provider");
        user.setAvatarUrl("https://example.com/old.png");
        user.setRealName("real name");
        when(userDao.selectById(1002L)).thenReturn(user);

        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(1002L);
        profile.setGender(1);
        profile.setVerifyStatus(2);
        profile.setDepositAmount(BigDecimal.ZERO);
        profile.setCreditScore(90);
        profile.setServiceRadiusKm(5);
        profile.setCertLabelsJson("[\"manual-old\"]");
        when(sitterProfileDao.selectById(1002L)).thenReturn(profile);

        SitterTrainingRecordDO trainingRecord = new SitterTrainingRecordDO();
        trainingRecord.setProviderId(1002L);
        trainingRecord.setLastExamPassed(1);
        when(sitterTrainingRecordDao.selectById(1002L)).thenReturn(trainingRecord);
        when(reviewDao.countByTargetIdAndType(1002L, 1)).thenReturn(0L);
        when(orderDao.countByProviderIdAndStatus(1002L, 6)).thenReturn(12L);

        CaretakerProfileRespDTO result = caretakerProfileService.updateMyProfile(
                new CaretakerProfileUpdateReqDTO(
                        "provider-new",
                        "https://example.com/new.png",
                        List.of("平台认证", "manual-new", "fake certificate"),
                        8,
                        "Shanghai Demo Road",
                        31.2,
                        121.4));

        assertEquals(List.of("manual-new", "fake certificate", "平台认证", "10+次服务"), result.certLabels());
        ArgumentCaptor<SitterProfileDO> profileCaptor = ArgumentCaptor.forClass(SitterProfileDO.class);
        verify(sitterProfileDao).updateById(profileCaptor.capture());
        assertEquals("[\"manual-new\",\"fake certificate\"]", profileCaptor.getValue().getCertLabelsJson());
        verify(userDao).updateById(any(UserDO.class));
    }
}
