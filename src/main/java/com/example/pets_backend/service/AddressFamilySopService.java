package com.example.pets_backend.service;

import com.example.pets_backend.dao.UserAddressDao;
import com.example.pets_backend.dao.entity.UserAddressDO;
import com.example.pets_backend.dto.req.AddressFamilySopReqDTO;
import com.example.pets_backend.dto.resp.AddressFamilySopRespDTO;
import com.example.pets_backend.enums.OrderHardFilterTagEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OrderHardFilterService;
import com.example.pets_backend.service.support.OrderRequirementTagService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AddressFamilySopService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserAddressDao userAddressDao;
    private final OrderRequirementTagService orderRequirementTagService;
    private final OrderHardFilterService orderHardFilterService;

    public AddressFamilySopRespDTO getFamilySop(Long addressId) {
        UserAddressDO address = getOwnedAddressOrThrow(addressId);
        return toRespDTO(address);
    }

    @Transactional
    public AddressFamilySopRespDTO saveFamilySop(Long addressId, AddressFamilySopReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        UserAddressDO address = getOwnedAddressOrThrow(addressId);
        String requirementJson = orderRequirementTagService.serialize(reqDTO.requirementTags());
        String hardFilterJson = orderHardFilterService.serializeTags(reqDTO.hardFilterTags());
        String remark = normalizeRemark(reqDTO.remark());
        if (!hasSopContent(requirementJson, hardFilterJson, remark)) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        address.setSopRequirementTagsJson(requirementJson);
        address.setSopHardFilterTags(hardFilterJson);
        address.setSopRemark(remark);
        address.setSopUpdatedAt(LocalDateTime.now());
        int updated = userAddressDao.updateFamilySopByAddressIdAndUserId(address);
        if (updated == 0) {
            throw new ClientException(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR);
        }
        return getFamilySop(addressId);
    }

    public boolean hasFamilySop(Long addressId) {
        UserAddressDO address = getOwnedAddressOrThrow(addressId);
        return hasSopContent(
                address.getSopRequirementTagsJson(),
                address.getSopHardFilterTags(),
                address.getSopRemark());
    }

    private UserAddressDO getOwnedAddressOrThrow(Long addressId) {
        if (addressId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        UserAddressDO address = userAddressDao.selectByAddressIdAndUserId(addressId, userId);
        if (address == null) {
            throw new ClientException(BaseErrorCode.USER_ADDRESS_NOT_FOUND_ERROR);
        }
        return address;
    }

    private AddressFamilySopRespDTO toRespDTO(UserAddressDO address) {
        List<String> hardFilterTags = orderHardFilterService.parseTags(address.getSopHardFilterTags());
        boolean hasSop = hasSopContent(
                address.getSopRequirementTagsJson(),
                address.getSopHardFilterTags(),
                address.getSopRemark());
        return new AddressFamilySopRespDTO(
                address.getAddressId(),
                hasSop,
                orderRequirementTagService.toRespDTO(address.getSopRequirementTagsJson()),
                hardFilterTags,
                OrderHardFilterTagEnum.describeTags(hardFilterTags),
                address.getSopRemark(),
                address.getSopUpdatedAt() == null ? null : DATE_TIME_FORMATTER.format(address.getSopUpdatedAt()));
    }

    private boolean hasSopContent(String requirementJson, String hardFilterJson, String remark) {
        var requirementSnapshot = orderRequirementTagService.parseSnapshot(requirementJson);
        List<String> hardFilterTags = orderHardFilterService.parseTags(hardFilterJson);
        return !requirementSnapshot.isEmpty()
                || (hardFilterTags != null && !hardFilterTags.isEmpty())
                || StringUtils.hasText(remark);
    }

    private String normalizeRemark(String remark) {
        if (!StringUtils.hasText(remark)) {
            return null;
        }
        return remark.trim();
    }
}
