package com.example.pets_backend.service;

import com.example.pets_backend.config.OfficialMessageProperties;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAccessService {

    private final OfficialMessageProperties officialMessageProperties;

    public void ensureAdmin() {
        Long currentUserId = UserContext.getUserId();
        if (currentUserId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        if (!currentUserId.equals(officialMessageProperties.getSystemSenderId())) {
            throw new ClientException(BaseErrorCode.CLIENT_ERROR);
        }
    }
}
