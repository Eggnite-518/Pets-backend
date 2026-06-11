package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SitterTrainingRecordDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.resp.UpgradeCaretakerRoleRespDTO;
import com.example.pets_backend.frameworks.auth.JwtUtil;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private SitterProfileDao sitterProfileDao;

    @Mock
    private SitterTrainingRecordDao sitterTrainingRecordDao;

    @Mock
    private JwtUtil jwtUtil;

    private UserRoleService userRoleService;

    @BeforeEach
    void setUp() {
        userRoleService = new UserRoleService(userDao, sitterProfileDao, sitterTrainingRecordDao, jwtUtil);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000000", "owner", 1, "old-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void upgradeOwnerToBothAndReturnFreshToken() {
        UserDO user = new UserDO();
        user.setUserId(1001L);
        user.setPhone("13800000000");
        user.setNickname("owner");
        user.setRoleType(1);
        when(userDao.selectById(1001L)).thenReturn(user);
        when(sitterProfileDao.selectById(1001L)).thenReturn(null);
        when(sitterTrainingRecordDao.selectById(1001L)).thenReturn(null);
        when(jwtUtil.generateAccessToken(any())).thenReturn("new-token");

        UpgradeCaretakerRoleRespDTO result = userRoleService.upgradeCurrentUserToCaretakerTemporarily();

        assertEquals(1001L, result.userId());
        assertEquals(3, result.roleType());
        assertEquals("new-token", result.token());
        assertEquals(0, result.verifyStatus());
        verify(userDao).updateById(user);
        verify(sitterProfileDao).insert(any(SitterProfileDO.class));
        verify(sitterTrainingRecordDao).insert(any());
    }

    @Test
    void keepBothRoleWithoutRedundantUserUpdate() {
        UserDO user = new UserDO();
        user.setUserId(1001L);
        user.setPhone("13800000000");
        user.setNickname("owner");
        user.setRoleType(3);
        SitterProfileDO profile = new SitterProfileDO();
        profile.setProviderId(1001L);
        profile.setVerifyStatus(2);
        when(userDao.selectById(1001L)).thenReturn(user);
        when(sitterProfileDao.selectById(1001L)).thenReturn(profile);
        when(sitterTrainingRecordDao.selectById(1001L)).thenReturn(new com.example.pets_backend.dao.entity.SitterTrainingRecordDO());
        when(jwtUtil.generateAccessToken(any())).thenReturn("new-token");

        UpgradeCaretakerRoleRespDTO result = userRoleService.upgradeCurrentUserToCaretakerTemporarily();

        assertEquals(3, result.roleType());
        assertEquals(2, result.verifyStatus());
        assertNotNull(result.roleTypeDesc());
        verify(userDao, never()).updateById(any());
    }
}
