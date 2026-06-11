package com.example.pets_backend.service;

import com.example.pets_backend.dao.PetArchiveDao;
import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dto.req.PetArchiveReqDTO;
import com.example.pets_backend.dto.req.PetProfileTagsReqDTO;
import com.example.pets_backend.dto.resp.PetArchiveRespDTO;
import com.example.pets_backend.enums.PetTypeEnum;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OssAccessibleUrlService;
import com.example.pets_backend.service.support.PetProfileTagService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PetArchiveService {

    private static final int PET_NAME_MAX_LENGTH = 50;

    private final PetArchiveDao petArchiveDao;
    private final PetProfileTagService petProfileTagService;
    private final OssAccessibleUrlService ossAccessibleUrlService;

    public PetArchiveRespDTO detail(Long petId) {
        PetArchiveDO petArchive = getOwnedPetArchiveOrThrow(petId);
        return toRespDTO(petArchive);
    }

    public List<PetArchiveRespDTO> list(String petName, Integer petType) {
        Long ownerId = currentUserId();
        String normalizedPetName = normalizeOptionalText(petName);
        validatePetTypeIfPresent(petType);

        return petArchiveDao.selectListByOwnerId(ownerId, normalizedPetName, petType)
                .stream()
                .map(this::toRespDTO)
                .toList();
    }

    @Transactional
    public PetArchiveRespDTO update(Long petId, PetArchiveReqDTO reqDTO) {
        if (petId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validateUpsertRequest(reqDTO.petName(), reqDTO.petType(), reqDTO.profileTags());

        Long ownerId = currentUserId();
        PetArchiveDO petArchive = new PetArchiveDO();
        petArchive.setPetId(petId);
        petArchive.setOwnerId(ownerId);
        petArchive.setPetName(normalizeRequiredText(reqDTO.petName()));
        petArchive.setPetType(reqDTO.petType());
        petArchive.setDefaultReq(normalizeOptionalText(reqDTO.defaultReq()));
        petArchive.setImage(normalizeOptionalImage(reqDTO.image()));
        petArchive.setProfileTagsJson(serializeProfileTags(reqDTO.profileTags()));

        int updatedRows = petArchiveDao.updateByPetIdAndOwnerId(petArchive);
        if (updatedRows == 0) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_NOT_FOUND_ERROR);
        }
        return detail(petId);
    }

    @Transactional
    public void delete(Long petId) {
        if (petId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long ownerId = currentUserId();
        int deletedRows = petArchiveDao.deleteByPetIdAndOwnerId(petId, ownerId);
        if (deletedRows == 0) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_NOT_FOUND_ERROR);
        }
    }

    private PetArchiveDO getOwnedPetArchiveOrThrow(Long petId) {
        if (petId == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        Long ownerId = currentUserId();
        PetArchiveDO petArchive = petArchiveDao.selectByPetIdAndOwnerId(petId, ownerId);
        if (petArchive == null) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_NOT_FOUND_ERROR);
        }
        return petArchive;
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    public PetArchiveRespDTO create(PetArchiveReqDTO reqDTO) {
        if (reqDTO == null) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
        validateUpsertRequest(reqDTO.petName(), reqDTO.petType(), reqDTO.profileTags());

        Long ownerId = currentUserId();
        PetArchiveDO petArchive = new PetArchiveDO();
        petArchive.setOwnerId(ownerId);
        petArchive.setPetName(normalizeRequiredText(reqDTO.petName()));
        petArchive.setPetType(reqDTO.petType());
        petArchive.setDefaultReq(normalizeOptionalText(reqDTO.defaultReq()));
        petArchive.setImage(normalizeOptionalImage(reqDTO.image()));
        petArchive.setProfileTagsJson(serializeProfileTags(reqDTO.profileTags()));

        petArchiveDao.insert(petArchive);
        return toRespDTO(petArchive);
    }

    private void validateUpsertRequest(String petName, Integer petType, PetProfileTagsReqDTO profileTags) {
        validatePetName(petName);
        validatePetType(petType);
        petProfileTagService.normalize(profileTags);
    }

    private void validatePetName(String petName) {
        if (!StringUtils.hasText(petName)) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_NAME_VERIFY_ERROR);
        }
        if (petName.trim().length() > PET_NAME_MAX_LENGTH) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_NAME_VERIFY_ERROR);
        }
    }

    private void validatePetType(Integer petType) {
        if (!PetTypeEnum.contains(petType)) {
            throw new ClientException(BaseErrorCode.PET_ARCHIVE_TYPE_VERIFY_ERROR);
        }
    }

    private void validatePetTypeIfPresent(Integer petType) {
        if (petType != null) {
            validatePetType(petType);
        }
    }

    private String normalizeRequiredText(String text) {
        return text == null ? null : text.trim();
    }

    private String normalizeOptionalText(String text) {
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return text.trim();
    }

    private String serializeProfileTags(PetProfileTagsReqDTO profileTags) {
        return petProfileTagService.serialize(profileTags);
    }

    private String normalizeOptionalImage(String image) {
        if (!StringUtils.hasText(image)) {
            return null;
        }
        return ossAccessibleUrlService.normalizeForStorage(image);
    }

    private PetArchiveRespDTO toRespDTO(PetArchiveDO petArchive) {
        return new PetArchiveRespDTO(
                petArchive.getPetId(),
                petArchive.getOwnerId(),
                petArchive.getPetName(),
                petArchive.getPetType(),
                PetTypeEnum.getDescByCode(petArchive.getPetType()),
                petArchive.getDefaultReq(),
                ossAccessibleUrlService.toDisplayUrl(petArchive.getImage()),
                petProfileTagService.toRespDTO(petArchive.getProfileTagsJson()));
    }
}
