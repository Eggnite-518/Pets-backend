package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.UserAddressDao;
import com.example.pets_backend.dao.entity.UserAddressDO;
import com.example.pets_backend.dto.req.UserAddressReqDTO;
import com.example.pets_backend.dto.resp.UserAddressRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTest {

    @Mock
    private UserAddressDao userAddressDao;

    private UserAddressService userAddressService;

    @BeforeEach
    void setUp() {
        userAddressService = new UserAddressService(userAddressDao);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "小林", 1, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void createUsesCurrentUserAndResetsDefaultWhenNeeded() {
        when(userAddressDao.insert(any())).thenAnswer(invocation -> {
            UserAddressDO userAddress = invocation.getArgument(0);
            userAddress.setAddressId(5001L);
            return 1;
        });

        UserAddressRespDTO resp = userAddressService.create(new UserAddressReqDTO(
                "张三",
                "13800000002",
                "广东省",
                "深圳市",
                "南山区",
                "科技园1号",
                "家庭",
                1,
                39.9042000,
                116.4074000));

        assertEquals(5001L, resp.addressId());
        assertEquals(1001L, resp.userId());
        assertEquals("张三", resp.contactName());
        assertEquals(1, resp.isDefault());
        assertEquals(39.9042000, resp.latitude(), 0.0000001);
        assertEquals(116.4074000, resp.longitude(), 0.0000001);
        verify(userAddressDao).resetDefaultByUserId(1001L);
        verify(userAddressDao).insert(any());
    }

    @Test
    void createRejectsInvalidRequest() {
        ClientException exception = assertThrows(ClientException.class,
                () -> userAddressService.create(new UserAddressReqDTO(
                        "",
                        "13800000002",
                        "广东省",
                        "深圳市",
                        "南山区",
                        "科技园1号",
                        null,
                        0,
                        null,
                        null)));

        assertEquals(BaseErrorCode.USER_ADDRESS_VERIFY_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void createRejectsMissingUserContext() {
        UserContext.clear();

        ClientException exception = assertThrows(ClientException.class,
                () -> userAddressService.create(new UserAddressReqDTO(
                        "张三",
                        "13800000002",
                        "广东省",
                        "深圳市",
                        "南山区",
                        "科技园1号",
                        null,
                        0,
                        null,
                        null)));

        assertEquals(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void detailRejectsCrossOwnerAccess() {
        when(userAddressDao.selectByAddressIdAndUserId(5001L, 1001L)).thenReturn(null);

        ClientException exception = assertThrows(ClientException.class,
                () -> userAddressService.detail(5001L));

        assertEquals(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void updateReturnsUpdatedAddress() {
        when(userAddressDao.updateByAddressIdAndUserId(any())).thenReturn(1);
        when(userAddressDao.selectByAddressIdAndUserId(5001L, 1001L))
                .thenReturn(buildAddress(5001L, 1001L, "张三", "13800000002", "广东省", "深圳市", "南山区", "科技园1号", "家庭", 1, 39.9042000, 116.4074000));

        UserAddressRespDTO resp = userAddressService.update(5001L, new UserAddressReqDTO(
                "张三",
                "13800000002",
                "广东省",
                "深圳市",
                "南山区",
                "科技园1号",
                "家庭",
                1,
                39.9042000,
                116.4074000));

        assertEquals(5001L, resp.addressId());
        assertEquals(1001L, resp.userId());
        assertEquals("科技园1号", resp.detailAddress());
        assertEquals(39.9042000, resp.latitude(), 0.0000001);
        assertEquals(116.4074000, resp.longitude(), 0.0000001);
        verify(userAddressDao).resetDefaultByUserId(1001L);
    }

    @Test
    void deleteRejectsMissingRecord() {
        when(userAddressDao.deleteByAddressIdAndUserId(5001L, 1001L)).thenReturn(0);

        ClientException exception = assertThrows(ClientException.class,
                () -> userAddressService.delete(5001L));

        assertEquals(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void switchDefaultUpdatesOwnedAddress() {
        when(userAddressDao.selectByAddressIdAndUserId(5001L, 1001L))
                .thenReturn(
                        buildAddress(5001L, 1001L, "张三", "13800000002", "广东省", "深圳市", "南山区", "科技园1号", "家庭", 0, 39.9042000, 116.4074000),
                        buildAddress(5001L, 1001L, "张三", "13800000002", "广东省", "深圳市", "南山区", "科技园1号", "家庭", 1, 39.9042000, 116.4074000));
        when(userAddressDao.updateByAddressIdAndUserId(any())).thenReturn(1);

        UserAddressRespDTO resp = userAddressService.switchDefault(5001L);

        assertEquals(5001L, resp.addressId());
        assertEquals(1, resp.isDefault());
        assertEquals(39.9042000, resp.latitude(), 0.0000001);
        assertEquals(116.4074000, resp.longitude(), 0.0000001);
        verify(userAddressDao).resetDefaultByUserId(1001L);
        verify(userAddressDao).updateByAddressIdAndUserId(any());
    }

    @Test
    void switchDefaultReturnsCurrentAddressWhenAlreadyDefault() {
        when(userAddressDao.selectByAddressIdAndUserId(5001L, 1001L))
                .thenReturn(buildAddress(5001L, 1001L, "张三", "13800000002", "广东省", "深圳市", "南山区", "科技园1号", "家庭", 1, 39.9042000, 116.4074000));

        UserAddressRespDTO resp = userAddressService.switchDefault(5001L);

        assertEquals(5001L, resp.addressId());
        assertEquals(1, resp.isDefault());
        verify(userAddressDao, never()).resetDefaultByUserId(1001L);
        verify(userAddressDao, never()).updateByAddressIdAndUserId(any());
    }

    @Test
    void switchDefaultRejectsMissingRecord() {
        when(userAddressDao.selectByAddressIdAndUserId(5001L, 1001L)).thenReturn(null);

        ClientException exception = assertThrows(ClientException.class,
                () -> userAddressService.switchDefault(5001L));

        assertEquals(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void listReturnsOnlyOwnedAddresses() {
        when(userAddressDao.selectListByUserId(1001L)).thenReturn(List.of(
                buildAddress(5001L, 1001L, "张三", "13800000002", "广东省", "深圳市", "南山区", "科技园1号", "家庭", 1, 39.9042000, 116.4074000),
                buildAddress(5002L, 1001L, "李四", "13800000003", "广东省", "深圳市", "福田区", "购物公园", null, 0, 39.9142000, 116.4174000)));

        List<UserAddressRespDTO> resp = userAddressService.list();

        assertEquals(2, resp.size());
        assertEquals("张三", resp.get(0).contactName());
        assertEquals("李四", resp.get(1).contactName());
        verify(userAddressDao, never()).selectByAddressIdAndUserId(any(), any());
    }

    private UserAddressDO buildAddress(Long addressId,
                                       Long userId,
                                       String contactName,
                                       String contactPhone,
                                       String province,
                                       String city,
                                       String district,
                                       String detailAddress,
                                       String addressTag,
                                       Integer isDefault,
                                       Double latitude,
                                       Double longitude) {
        UserAddressDO userAddress = new UserAddressDO();
        userAddress.setAddressId(addressId);
        userAddress.setUserId(userId);
        userAddress.setContactName(contactName);
        userAddress.setContactPhone(contactPhone);
        userAddress.setProvince(province);
        userAddress.setCity(city);
        userAddress.setDistrict(district);
        userAddress.setDetailAddress(detailAddress);
        userAddress.setAddressTag(addressTag);
        userAddress.setIsDefault(isDefault);
        userAddress.setLatitude(latitude);
        userAddress.setLongitude(longitude);
        return userAddress;
    }
}



