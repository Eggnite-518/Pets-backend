package com.example.pets_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.pets_backend.dao.PetOwnerDao;
import com.example.pets_backend.dao.entity.PetOwnerDO;
import com.example.pets_backend.dto.req.PetOwnerReqDTO;
import com.example.pets_backend.dto.resp.PetOwnerRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.auth.UserInfoDTO;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PetOwnerServiceTest {

    @Mock
    private PetOwnerDao petOwnerDao;

    private PetOwnerService petOwnerService;

    @BeforeEach
    void setUp() {
        petOwnerService = new PetOwnerService(petOwnerDao);
        UserContext.setUser(new UserInfoDTO(1001L, "13800000001", "小林", 1, "jwt-token"));
    }

    @AfterEach
    void tearDown() {
        UserContext.clear();
    }

    @Test
    void createUsesCurrentUserAsOwnerId() {
        when(petOwnerDao.selectByOwnerId(1001L)).thenReturn(null);
        when(petOwnerDao.insert(any())).thenReturn(1);

        PetOwnerRespDTO resp = petOwnerService.create(new PetOwnerReqDTO("13800000002"));

        assertEquals(1001L, resp.ownerId());
        assertEquals("13800000002", resp.emergencyContact());
        verify(petOwnerDao).insert(any());
    }

    @Test
    void createRejectsDuplicatePetOwner() {
        when(petOwnerDao.selectByOwnerId(1001L)).thenReturn(buildPetOwner(1001L, "13800000002"));

        ClientException exception = assertThrows(ClientException.class,
                () -> petOwnerService.create(new PetOwnerReqDTO("13800000002")));

        assertEquals(BaseErrorCode.PET_OWNER_ALREADY_EXISTS_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void createRejectsInvalidEmergencyContact() {
        ClientException exception = assertThrows(ClientException.class,
                () -> petOwnerService.create(new PetOwnerReqDTO("123456789012345678901")));

        assertEquals(BaseErrorCode.PET_OWNER_VERIFY_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void createRejectsMissingUserContext() {
        UserContext.clear();

        ClientException exception = assertThrows(ClientException.class,
                () -> petOwnerService.create(new PetOwnerReqDTO("13800000002")));

        assertEquals(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR.code(), exception.getErrorCode());
    }

    @Test
    void detailReturnsPetOwner() {
        when(petOwnerDao.selectByOwnerId(1001L)).thenReturn(buildPetOwner(1001L, "13800000002"));

        PetOwnerRespDTO resp = petOwnerService.detail();

        assertEquals(1001L, resp.ownerId());
        assertEquals("13800000002", resp.emergencyContact());
    }

    @Test
    void updateReturnsUpdatedPetOwner() {
        when(petOwnerDao.updateByOwnerId(any())).thenReturn(1);
        when(petOwnerDao.selectByOwnerId(1001L)).thenReturn(buildPetOwner(1001L, "13900000003"));

        PetOwnerRespDTO resp = petOwnerService.update(new PetOwnerReqDTO("13900000003"));

        assertEquals(1001L, resp.ownerId());
        assertEquals("13900000003", resp.emergencyContact());
    }

    @Test
    void deleteRejectsMissingRecord() {
        when(petOwnerDao.deleteByOwnerId(1001L)).thenReturn(0);

        ClientException exception = assertThrows(ClientException.class,
                petOwnerService::delete);

        assertEquals(BaseErrorCode.PET_OWNER_NOT_FOUND_ERROR.code(), exception.getErrorCode());
    }

    private PetOwnerDO buildPetOwner(Long ownerId, String emergencyContact) {
        PetOwnerDO petOwner = new PetOwnerDO();
        petOwner.setOwnerId(ownerId);
        petOwner.setEmergencyContact(emergencyContact);
        return petOwner;
    }
}

