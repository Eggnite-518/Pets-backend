package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.QuestionBankDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SitterTrainingRecordDao;
import com.example.pets_backend.dao.TrainingMaterialDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.SitterTrainingRecordDO;
import com.example.pets_backend.dao.entity.TrainingMaterialDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.resp.TempCaretakerReadyRespDTO;
import com.example.pets_backend.dto.resp.TrainingStatusRespDTO;
import java.time.LocalDateTime;
import java.util.List;
import com.example.pets_backend.frameworks.auth.JwtUtil;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TrainingServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private SitterProfileDao sitterProfileDao;

    @Mock
    private SitterTrainingRecordDao sitterTrainingRecordDao;

    @Mock
    private TrainingMaterialDao trainingMaterialDao;

    @Mock
    private QuestionBankDao questionBankDao;

    @Mock
    private JwtUtil jwtUtil;

    private TrainingService trainingService;

    @BeforeEach
    void setUp() {
        trainingService = new TrainingService(
                userDao,
                sitterProfileDao,
                sitterTrainingRecordDao,
                trainingMaterialDao,
                questionBankDao,
                new ObjectMapper(),
                jwtUtil);
        UserContext.setUser(new UserInfoDTO(1002L, "13900000001", "caretaker", 3, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void getStatusUsesTrainingProgressEvenWhenJwtRoleMissing() {
        UserDO user = new UserDO();
        user.setUserId(1002L);
        user.setPhone("13900000001");
        user.setNickname("caretaker");
        user.setRoleType(2);
        user.setRealName("张三");
        user.setIdCardNo("110101199001011234");

        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(1002L);
        profile.setVerifyStatus(1);

        SitterTrainingRecordDO record = new SitterTrainingRecordDO();
        record.setProviderId(1002L);
        record.setLearningProgressJson("""
                {
                  "900001": {"watchedSeconds": 60, "completedAt": "2026-06-11T10:00:00"},
                  "900002": {"watchedSeconds": 90, "completedAt": "2026-06-11T10:05:00"}
                }
                """);

        TrainingMaterialDO material1 = new TrainingMaterialDO();
        material1.setMaterialId(900001L);
        material1.setTitle("平台服务总则");
        material1.setMinDurationSeconds(60);
        material1.setIsRequired(1);

        TrainingMaterialDO material2 = new TrainingMaterialDO();
        material2.setMaterialId(900002L);
        material2.setTitle("宠物行为学基础");
        material2.setMinDurationSeconds(90);
        material2.setIsRequired(1);

        UserContext.setUser(new UserInfoDTO(1002L, "13900000001", "caretaker", null, "jwt-token"));

        when(userDao.selectById(1002L)).thenReturn(user);
        when(sitterProfileDao.selectById(1002L)).thenReturn(profile);
        when(sitterTrainingRecordDao.selectById(1002L)).thenReturn(record);
        when(trainingMaterialDao.selectRequiredCurriculum()).thenReturn(List.of(material1, material2));

        TrainingStatusRespDTO status = trainingService.getStatus();

        assertEquals(2, status.requiredMaterialCount());
        assertEquals(2, status.completedMaterialCount());
        assertEquals(100, status.learningProgressPercent());
    }

    @Test
    void getStatusTreatsLearningCompletedAsFullProgress() {
        UserDO user = new UserDO();
        user.setUserId(1002L);
        user.setRoleType(2);
        user.setRealName("张三");
        user.setIdCardNo("110101199001011234");

        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(1002L);
        profile.setVerifyStatus(1);

        SitterTrainingRecordDO record = new SitterTrainingRecordDO();
        record.setProviderId(1002L);
        record.setLearningCompletedAt(LocalDateTime.now());
        record.setLearningProgressJson("{}");

        TrainingMaterialDO material = new TrainingMaterialDO();
        material.setMaterialId(900001L);
        material.setTitle("平台服务总则");
        material.setMinDurationSeconds(60);
        material.setIsRequired(1);

        when(userDao.selectById(1002L)).thenReturn(user);
        when(sitterProfileDao.selectById(1002L)).thenReturn(profile);
        when(sitterTrainingRecordDao.selectById(1002L)).thenReturn(record);
        when(trainingMaterialDao.selectRequiredCurriculum()).thenReturn(List.of(material));

        TrainingStatusRespDTO status = trainingService.getStatus();

        assertEquals(1, status.requiredMaterialCount());
        assertEquals(1, status.completedMaterialCount());
    }

    @Test
    void passTrainingTemporarilyMakesUserReadyForApplying() {
        UserDO user = new UserDO();
        user.setUserId(1002L);
        user.setPhone("13900000001");
        user.setNickname("caretaker");
        user.setRoleType(1);

        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(1002L);
        profile.setVerifyStatus(0);
        profile.setDepositAmount(BigDecimal.ZERO);

        SitterTrainingRecordDO record = new SitterTrainingRecordDO();
        record.setProviderId(1002L);

        when(userDao.selectById(1002L)).thenReturn(user);
        when(sitterProfileDao.selectById(1002L)).thenReturn(profile);
        when(sitterTrainingRecordDao.selectById(1002L)).thenReturn(record);
        when(jwtUtil.generateAccessToken(any())).thenReturn("new-caretaker-token");

        TempCaretakerReadyRespDTO result = trainingService.passTrainingTemporarily();

        assertEquals(2, result.verifyStatus());
        assertEquals(3, result.roleType());
        assertEquals("500.00", result.depositAmount());
        assertEquals("new-caretaker-token", result.token());
        assertTrue(Boolean.TRUE.equals(result.realNameVerified()));
        assertTrue(Boolean.TRUE.equals(result.canApplyOrder()));
        assertEquals(3, user.getRoleType());
        assertEquals("110101199001011002", user.getIdCardNo());
        verify(userDao).updateById(user);
        verify(sitterTrainingRecordDao).updateById(record);
        verify(sitterProfileDao).updateById(profile);
    }

    @Test
    void resetTrainingClearsProgressForPassedCaretaker() {
        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(1002L);
        profile.setVerifyStatus(2);

        when(sitterProfileDao.selectById(1002L)).thenReturn(profile);
        when(sitterTrainingRecordDao.selectById(1002L)).thenReturn(new SitterTrainingRecordDO());

        trainingService.resetTraining(1002L, "INACTIVE_180_DAYS");

        assertEquals(0, profile.getVerifyStatus());
        verify(sitterProfileDao).updateById(profile);
        verify(sitterTrainingRecordDao).resetProgressForProvider(1002L, "INACTIVE_180_DAYS");
    }

    @Test
    void resetTrainingSkipsCaretakerWhoNeverPassedExam() {
        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(1002L);
        profile.setVerifyStatus(0);

        when(sitterProfileDao.selectById(1002L)).thenReturn(profile);

        trainingService.resetTraining(1002L, "INACTIVE_180_DAYS");

        verify(sitterProfileDao, never()).updateById(any());
        verify(sitterTrainingRecordDao, never()).resetProgressForProvider(eq(1002L), any());
    }
}
