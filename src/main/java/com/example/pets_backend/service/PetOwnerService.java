package com.example.pets_backend.service;

import com.example.pets_backend.dao.PetOwnerDao;
import com.example.pets_backend.dao.UserDao;
import com.example.pets_backend.dao.entity.PetOwnerDO;
import com.example.pets_backend.dao.entity.UserDO;
import com.example.pets_backend.dto.req.PetOwnerReqDTO;
import com.example.pets_backend.dto.resp.PetOwnerRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PetOwnerService {

    private static final int EMERGENCY_CONTACT_MAX_LENGTH = 20;

    private final PetOwnerDao petOwnerDao;
    private final UserDao userDao;
    private final OssAccessibleUrlService ossAccessibleUrlService;

    @Transactional
    public PetOwnerRespDTO create(PetOwnerReqDTO reqDTO) {
        validateUpsertRequest(reqDTO);

        Long ownerId = currentUserId();
        PetOwnerDO existedPetOwner = petOwnerDao.selectByOwnerId(ownerId);
        if (existedPetOwner != null) {
            throw new ClientException(BaseErrorCode.PET_OWNER_ALREADY_EXISTS_ERROR);
        }

        PetOwnerDO petOwner = buildPetOwner(ownerId, reqDTO);
        petOwnerDao.insert(petOwner);
        return toRespDTO(getCurrentUserOrThrow(ownerId));
    }

    public PetOwnerRespDTO detail() {
        Long ownerId = currentUserId();
        UserDO user = getCurrentUserOrThrow(ownerId);
        return toRespDTO(user);
    }

    @Transactional
    public PetOwnerRespDTO update(PetOwnerReqDTO reqDTO) {
        validateUpsertRequest(reqDTO);

        Long ownerId = currentUserId();
        PetOwnerDO petOwner = buildPetOwner(ownerId, reqDTO);
        int updatedRows = petOwnerDao.updateByOwnerId(petOwner);
        if (updatedRows == 0) {
            throw new ClientException(BaseErrorCode.PET_OWNER_NOT_FOUND_ERROR);
        }
        return detail();
    }

    @Transactional
    public void delete() {
        Long ownerId = currentUserId();
        int deletedRows = petOwnerDao.deleteByOwnerId(ownerId);
        if (deletedRows == 0) {
            throw new ClientException(BaseErrorCode.PET_OWNER_NOT_FOUND_ERROR);
        }
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    private void validateUpsertRequest(PetOwnerReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (reqDTO.emergencyContact() != null && reqDTO.emergencyContact().trim().length() > EMERGENCY_CONTACT_MAX_LENGTH) {
            throw new ClientException(BaseErrorCode.PET_OWNER_VERIFY_ERROR);
        }
    }

    private PetOwnerDO buildPetOwner(Long ownerId, PetOwnerReqDTO reqDTO) {
        PetOwnerDO petOwner = new PetOwnerDO();
        petOwner.setOwnerId(ownerId);
        petOwner.setEmergencyContact(normalizeOptionalText(reqDTO.emergencyContact()));
        return petOwner;
    }

    private String normalizeOptionalText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    private UserDO getCurrentUserOrThrow(Long ownerId) {
        UserDO user = userDao.selectById(ownerId);
        if (user == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        return user;
    }

    private PetOwnerRespDTO toRespDTO(UserDO user) {
        return new PetOwnerRespDTO(
                user.getUserId(),
                user.getNickname(),
                ossAccessibleUrlService.toDisplayUrl(user.getAvatarUrl()),
                user.getPhone());
    }
}
