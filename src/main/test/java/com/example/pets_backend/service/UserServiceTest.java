package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.ArbitrationRecordDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.SmsVerificationCodeDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.LoginUserReqDTO;
import com.example.pets_backend.dto.resp.LoginUserRespDTO;
import com.example.pets_backend.frameworks.auth.JwtUtil;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.infrastructure.oss.ObjectStorageService;
import com.example.pets_backend.service.encoder.PasswordEncoder;
import com.example.pets_backend.service.sms.SmsService;
import com.example.pets_backend.service.support.IdCardGenderSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserDao userDao;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SmsVerificationCodeDao smsVerificationCodeDao;

    @Mock
    private SmsService smsService;

    @Mock
    private OrderDao orderDao;

    @Mock
    private ArbitrationRecordDao arbitrationRecordDao;

    @Mock
    private SitterProfileDao sitterProfileDao;

    @Mock
    private ObjectStorageService objectStorageService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDao, passwordEncoder, jwtUtil,
                smsVerificationCodeDao, smsService, orderDao, arbitrationRecordDao,
                sitterProfileDao, objectStorageService);
    }

    @Test
    void loginReturnsJwtWhenPasswordMatches() {
        UserDO user = new UserDO();
        user.setUserId(1001L);
        user.setPhone("13800000000");
        user.setNickname("宠物主人");
        user.setRoleType(1);
        user.setPasswordHash("hashed-password");

        when(userDao.selectByPhone("13800000000")).thenReturn(user);
        when(passwordEncoder.encrypt("Abc12345!")).thenReturn("hashed-password");
        when(jwtUtil.generateAccessToken(any())).thenReturn("jwt-token");

        LoginUserRespDTO resp = userService.login(new LoginUserReqDTO("13800000000", "Abc12345!"));

        assertEquals(1001L, resp.userId());
        assertEquals("宠物主人", resp.nickname());
        assertEquals("13800000000", resp.phone());
        assertEquals(1, resp.roleType());
        assertEquals("jwt-token", resp.token());
    }

    @Test
    void loginRejectsWrongCredentialsWithUnifiedError() {
        UserDO user = new UserDO();
        user.setUserId(1001L);
        user.setPhone("13800000000");
        user.setNickname("宠物主人");
        user.setRoleType(1);
        user.setPasswordHash("hashed-password");

        when(userDao.selectByPhone("13800000000")).thenReturn(user);
        when(passwordEncoder.encrypt("Wrong123!" )).thenReturn("other-hash");

        ClientException exception = assertThrows(ClientException.class,
                () -> userService.login(new LoginUserReqDTO("13800000000", "Wrong123!")));

        assertEquals(BaseErrorCode.USER_LOGIN_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void realNameVerifySetsCaretakerGenderFromIdCard() {
        UserDO user = new UserDO();
        user.setUserId(2001L);
        user.setRoleType(2);

        when(userDao.selectById(2001L)).thenReturn(user);
        when(sitterProfileDao.selectById(2001L)).thenReturn(null);

        userService.realNameVerify(2001L, "张三", "110101199002021234", null, null);

        verify(sitterProfileDao).insert(argThat(profile ->
                profile.getGender() != null
                        && profile.getGender() == IdCardGenderSupport.GENDER_FEMALE));
    }
}

