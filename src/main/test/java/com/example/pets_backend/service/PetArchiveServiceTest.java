package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.PetArchiveDao;
import com.example.pets_backend.dao.entity.PetArchiveDO;
import com.example.pets_backend.dto.req.PetArchiveReqDTO;
import com.example.pets_backend.dto.req.PetProfileTagsReqDTO;
import com.example.pets_backend.dto.resp.PetArchiveRespDTO;
import java.math.BigDecimal;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.PetProfileTagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetArchiveServiceTest {

    @Mock
    private PetArchiveDao petArchiveDao;

    private PetArchiveService petArchiveService;

    @BeforeEach
    void setUp() {
        petArchiveService = new PetArchiveService(
                petArchiveDao, new PetProfileTagService(new ObjectMapper()));
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "小林", 1, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void createUsesCurrentUserAsOwnerId() {
        when(petArchiveDao.insert(any())).thenAnswer(invocation -> {
            PetArchiveDO petArchive = invocation.getArgument(0);
            petArchive.setPetId(3001L);
            return 1;
        });

        PetArchiveRespDTO resp = petArchiveService.create(new PetArchiveReqDTO(
                "团团",
                1,
                "每日更换清水",
                null,
                null));

        assertEquals(3001L, resp.petId());
        assertEquals(1001L, resp.ownerId());
        assertEquals("团团", resp.petName());
        assertEquals(1, resp.petType());
        assertEquals("每日更换清水", resp.defaultReq());
        verify(petArchiveDao).insert(any());
    }

    @Test
    void createRejectsInvalidPetType() {
        ClientException exception = assertThrows(ClientException.class,
                () -> petArchiveService.create(new PetArchiveReqDTO("团团", 9, "每日更换清水", null, null)));

        assertEquals(BaseErrorCode.PET_ARCHIVE_TYPE_VERIFY_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void createRejectsMissingUserContext() {
        UserContext.clear();

        ClientException exception = assertThrows(ClientException.class,
                () -> petArchiveService.create(new PetArchiveReqDTO("团团", 1, "每日更换清水", null, null)));

        assertEquals(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void detailRejectsCrossOwnerAccess() {
        when(petArchiveDao.selectByPetIdAndOwnerId(3001L, 1001L)).thenReturn(null);

        ClientException exception = assertThrows(ClientException.class,
                () -> petArchiveService.detail(3001L));

        assertEquals(BaseErrorCode.PET_ARCHIVE_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void updateReturnsUpdatedPetArchive() {
        when(petArchiveDao.updateByPetIdAndOwnerId(any())).thenReturn(1);
        when(petArchiveDao.selectByPetIdAndOwnerId(3001L, 1001L)).thenReturn(buildPetArchive(3001L, "可乐", 2, "遛狗20分钟"));

        PetArchiveRespDTO resp = petArchiveService.update(3001L, new PetArchiveReqDTO(
                "可乐",
                2,
                "遛狗20分钟",
                null,
                null));

        assertEquals(3001L, resp.petId());
        assertEquals(1001L, resp.ownerId());
        assertEquals("可乐", resp.petName());
        assertEquals(2, resp.petType());
        assertEquals("遛狗20分钟", resp.defaultReq());
    }

    @Test
    void deleteRejectsMissingRecord() {
        when(petArchiveDao.deleteByPetIdAndOwnerId(3001L, 1001L)).thenReturn(0);

        ClientException exception = assertThrows(ClientException.class,
                () -> petArchiveService.delete(3001L));

        assertEquals(BaseErrorCode.PET_ARCHIVE_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void createSupportsExoticPetTypeAndProfileTags() {
        when(petArchiveDao.insert(any())).thenAnswer(invocation -> {
            PetArchiveDO petArchive = invocation.getArgument(0);
            petArchive.setPetId(3003L);
            return 1;
        });

        PetArchiveRespDTO resp = petArchiveService.create(new PetArchiveReqDTO(
                "龙猫",
                3,
                "每日更换垫料",
                null,
                new PetProfileTagsReqDTO(
                        new BigDecimal("0.45"),
                        "ADULT",
                        List.of("NEUTERED"),
                        3,
                        "LOW",
                        List.of(),
                        List.of("HIDING"),
                        List.of("MEDICATION"))));

        assertEquals(3, resp.petType());
        assertEquals("异宠", resp.petTypeDesc());
        assertNotNull(resp.profileTags());
        assertEquals("成年期", resp.profileTags().ageGroupDesc());
        assertEquals(List.of("用药需求"), resp.profileTags().healthTagDescs());
    }

    @Test
    void listReturnsOnlyOwnedPetArchives() {
        when(petArchiveDao.selectListByOwnerId(1001L, "团", 1)).thenReturn(List.of(
                buildPetArchive(3001L, "团团", 1, "清水"),
                buildPetArchive(3002L, "团子", 1, "少量湿粮")));

        List<PetArchiveRespDTO> resp = petArchiveService.list("团", 1);

        assertEquals(2, resp.size());
        assertEquals("团团", resp.get(0).petName());
        assertEquals("团子", resp.get(1).petName());
    }

    private PetArchiveDO buildPetArchive(Long petId, String petName, Integer petType, String defaultReq) {
        PetArchiveDO petArchive = new PetArchiveDO();
        petArchive.setPetId(petId);
        petArchive.setOwnerId(1001L);
        petArchive.setPetName(petName);
        petArchive.setPetType(petType);
        petArchive.setDefaultReq(defaultReq);
        return petArchive;
    }
}




