package com.example.pets_backend.service;

import com.example.pets_backend.dao.UserAddressDao;
import com.example.pets_backend.dao.entity.UserAddressDO;
import com.example.pets_backend.dto.req.UserAddressReqDTO;
import com.example.pets_backend.dto.resp.UserAddressRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private static final Pattern MAINLAND_PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final int CONTACT_NAME_MAX_LENGTH = 50;
    private static final int PROVINCE_MAX_LENGTH = 50;
    private static final int CITY_MAX_LENGTH = 50;
    private static final int DISTRICT_MAX_LENGTH = 50;
    private static final int DETAIL_ADDRESS_MAX_LENGTH = 255;
    private static final int ADDRESS_TAG_MAX_LENGTH = 30;
    private static final double LATITUDE_MIN = -90.0;
    private static final double LATITUDE_MAX = 90.0;
    private static final double LONGITUDE_MIN = -180.0;
    private static final double LONGITUDE_MAX = 180.0;
    private static final int DEFAULT_FLAG_NO = 0;
    private static final int DEFAULT_FLAG_YES = 1;

    private final UserAddressDao userAddressDao;

    @Transactional
    public UserAddressRespDTO create(UserAddressReqDTO reqDTO) {
        validateUpsertRequest(reqDTO);

        Long userId = currentUserId();
        UserAddressDO userAddress = buildUserAddress(userId, reqDTO);
        if (isDefaultAddress(reqDTO.isDefault())) {
            userAddressDao.resetDefaultByUserId(userId);
        }
        userAddressDao.insert(userAddress);
        return toRespDTO(userAddress);
    }

    public UserAddressRespDTO detail(Long addressId) {
        return toRespDTO(getOwnedUserAddressOrThrow(addressId));
    }

    public List<UserAddressRespDTO> list() {
        Long userId = currentUserId();
        return userAddressDao.selectListByUserId(userId)
                .stream()
                .map(this::toRespDTO)
                .toList();
    }

    @Transactional
    public UserAddressRespDTO update(Long addressId, UserAddressReqDTO reqDTO) {
        if (addressId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validateUpsertRequest(reqDTO);

        Long userId = currentUserId();
        UserAddressDO userAddress = buildUserAddress(userId, reqDTO);
        userAddress.setAddressId(addressId);
        if (isDefaultAddress(reqDTO.isDefault())) {
            userAddressDao.resetDefaultByUserId(userId);
        }

        int updatedRows = userAddressDao.updateByAddressIdAndUserId(userAddress);
        if (updatedRows == 0) {
            throw new ClientException(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR);
        }
        return detail(addressId);
    }

    @Transactional
    public void delete(Long addressId) {
        if (addressId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = currentUserId();
        int deletedRows = userAddressDao.deleteByAddressIdAndUserId(addressId, userId);
        if (deletedRows == 0) {
            throw new ClientException(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR);
        }
    }

    @Transactional
    public UserAddressRespDTO switchDefault(Long addressId) {
        UserAddressDO userAddress = getOwnedUserAddressOrThrow(addressId);
        if (Objects.equals(userAddress.getIsDefault(), DEFAULT_FLAG_YES)) {
            return toRespDTO(userAddress);
        }

        Long userId = currentUserId();
        userAddressDao.resetDefaultByUserId(userId);

        userAddress.setIsDefault(DEFAULT_FLAG_YES);
        int updatedRows = userAddressDao.updateByAddressIdAndUserId(userAddress);
        if (updatedRows == 0) {
            throw new ClientException(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR);
        }
        return detail(addressId);
    }

    private UserAddressDO getOwnedUserAddressOrThrow(Long addressId) {
        if (addressId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = currentUserId();
        UserAddressDO userAddress = userAddressDao.selectByAddressIdAndUserId(addressId, userId);
        if (userAddress == null) {
            throw new ClientException(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR);
        }
        return userAddress;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    private void validateUpsertRequest(UserAddressReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validateRequiredText(reqDTO.contactName(), CONTACT_NAME_MAX_LENGTH, "联系人");
        validatePhone(reqDTO.contactPhone());
        validateRequiredText(reqDTO.province(), PROVINCE_MAX_LENGTH, "省份");
        validateRequiredText(reqDTO.city(), CITY_MAX_LENGTH, "城市");
        validateRequiredText(reqDTO.district(), DISTRICT_MAX_LENGTH, "区/县");
        validateRequiredText(reqDTO.detailAddress(), DETAIL_ADDRESS_MAX_LENGTH, "详细地址");
        validateCoordinates(reqDTO.latitude(), reqDTO.longitude());
        validateAddressTag(reqDTO.addressTag());
        validateIsDefault(reqDTO.isDefault());
    }

    private void validateRequiredText(String text, int maxLength, String fieldName) {
        if (!StringUtils.hasText(text)) {
            throw new ClientException(fieldName + "不能为空", BaseErrorCode.USER_ADDRESS_VERIFY_ERROR);
        }
        if (text.trim().length() > maxLength) {
            throw new ClientException(fieldName + "长度不能超过" + maxLength + "个字符", BaseErrorCode.USER_ADDRESS_VERIFY_ERROR);
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || !MAINLAND_PHONE_PATTERN.matcher(phone).matches()) {
            throw new ClientException(BaseErrorCode.PHONE_VERIFY_ERROR);
        }
    }

    private boolean isDefaultAddress(Integer isDefault) {
        return Objects.equals(isDefault, DEFAULT_FLAG_YES);
    }

    private void validateAddressTag(String addressTag) {
        if (addressTag != null && addressTag.trim().length() > ADDRESS_TAG_MAX_LENGTH) {
            throw new ClientException("地址标签长度不能超过" + ADDRESS_TAG_MAX_LENGTH + "个字符", BaseErrorCode.USER_ADDRESS_VERIFY_ERROR);
        }
    }

    private void validateIsDefault(Integer isDefault) {
        if (isDefault != null
                && !Objects.equals(isDefault, DEFAULT_FLAG_NO)
                && !Objects.equals(isDefault, DEFAULT_FLAG_YES)) {
            throw new ClientException("是否默认地址只能是0或1", BaseErrorCode.USER_ADDRESS_VERIFY_ERROR);
        }
    }

    private void validateCoordinates(Double latitude, Double longitude) {
        if (latitude == null && longitude == null) {
            return;
        }
        if (latitude == null || longitude == null) {
            throw new ClientException("地址坐标需同时传入纬度和经度", BaseErrorCode.USER_ADDRESS_VERIFY_ERROR);
        }
        if (latitude < LATITUDE_MIN || latitude > LATITUDE_MAX
                || longitude < LONGITUDE_MIN || longitude > LONGITUDE_MAX) {
            throw new ClientException("地址坐标不合法", BaseErrorCode.USER_ADDRESS_VERIFY_ERROR);
        }
    }

    private UserAddressDO buildUserAddress(Long userId, UserAddressReqDTO reqDTO) {
        UserAddressDO userAddress = new UserAddressDO();
        userAddress.setUserId(userId);
        userAddress.setContactName(reqDTO.contactName().trim());
        userAddress.setContactPhone(reqDTO.contactPhone().trim());
        userAddress.setProvince(reqDTO.province().trim());
        userAddress.setCity(reqDTO.city().trim());
        userAddress.setDistrict(reqDTO.district().trim());
        userAddress.setDetailAddress(reqDTO.detailAddress().trim());
        userAddress.setAddressTag(normalizeOptionalText(reqDTO.addressTag()));
        userAddress.setIsDefault(reqDTO.isDefault() == null ? DEFAULT_FLAG_NO : reqDTO.isDefault());
        userAddress.setLatitude(reqDTO.latitude());
        userAddress.setLongitude(reqDTO.longitude());
        return userAddress;
    }

    private String normalizeOptionalText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    private UserAddressRespDTO toRespDTO(UserAddressDO userAddress) {
        return new UserAddressRespDTO(
                userAddress.getAddressId(),
                userAddress.getUserId(),
                userAddress.getContactName(),
                userAddress.getContactPhone(),
                userAddress.getProvince(),
                userAddress.getCity(),
                userAddress.getDistrict(),
                userAddress.getDetailAddress(),
                userAddress.getAddressTag(),
                userAddress.getIsDefault(),
                userAddress.getLatitude(),
                userAddress.getLongitude());
    }
}


