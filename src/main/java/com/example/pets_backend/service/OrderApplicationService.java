package com.example.pets_backend.service;

import com.example.pets_backend.dao.OrderAddressSnapshotDao;
import com.example.pets_backend.dao.OrderApplicationDao;
import com.example.pets_backend.dao.OrderDao;
import com.example.pets_backend.dao.SitterProfileDao;
import com.example.pets_backend.dao.entity.OrderApplicationDO;
import com.example.pets_backend.dao.entity.OrderAddressSnapshotDO;
import com.example.pets_backend.dao.entity.OrderDO;
import com.example.pets_backend.dao.entity.SitterProfileDO;
import com.example.pets_backend.dto.resp.OrderApplicationRespDTO;
import com.example.pets_backend.frameworks.auth.UserContext;
import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.ClientException;
import com.example.pets_backend.service.support.OrderHardFilterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderApplicationService {

    private static final int ORDER_STATUS_BOUNTY = 1;
    private static final int APPLY_STATUS_APPLYING = 0;
    private static final int ROLE_CARETAKER = 2;
    private static final int ROLE_BOTH = 3;
    private static final int VERIFY_STATUS_PASSED = 2;

    private final OrderDao orderDao;
    private final OrderApplicationDao orderApplicationDao;
    private final OrderAddressSnapshotDao orderAddressSnapshotDao;
    private final SitterProfileDao sitterProfileDao;
    private final CaretakerDepositService caretakerDepositService;
    private final OrderHardFilterService orderHardFilterService;
    private final OrderOfficialNotificationService orderOfficialNotificationService;

    @Transactional
    public OrderApplicationRespDTO apply(Long orderId) {
        Long providerId = currentUserId();
        Integer roleType = currentUserRoleType();

        if (roleType == null || (roleType != ROLE_CARETAKER && roleType != ROLE_BOTH)) {
            throw new ClientException(BaseErrorCode.CARETAKER_ROLE_REQUIRED_ERROR);
        }

        SitterProfileDO profile = sitterProfileDao.selectById(providerId);
        if (profile == null || profile.getVerifyStatus() == null || profile.getVerifyStatus() != VERIFY_STATUS_PASSED) {
            throw new ClientException(BaseErrorCode.CARETAKER_VERIFY_REQUIRED_ERROR);
        }

        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }

        if (order.getStatus() == null || order.getStatus() != ORDER_STATUS_BOUNTY) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OPEN_ERROR);
        }

        if (order.getOwnerId().equals(providerId)) {
            throw new ClientException(BaseErrorCode.ORDER_OWNER_PROVIDER_CONFLICT_ERROR);
        }

        if (orderApplicationDao.existsByOrderIdAndProviderId(orderId, providerId)) {
            throw new ClientException(BaseErrorCode.ORDER_ALREADY_APPLIED_ERROR);
        }

        OrderAddressSnapshotDO addressSnapshot = orderAddressSnapshotDao.selectById(order.getAddressSnapshotId());
        orderHardFilterService.ensureProviderEligible(profile, addressSnapshot, order);

        caretakerDepositService.ensureDepositForOrder(profile.getDepositAmount(), orderId);

        OrderApplicationDO application = new OrderApplicationDO();
        application.setOrderId(orderId);
        application.setProviderId(providerId);
        application.setApplyStatus(APPLY_STATUS_APPLYING);

        orderApplicationDao.insert(application);
        orderOfficialNotificationService.notifyOwnerOnProviderApplication(order, providerId);
        return new OrderApplicationRespDTO(application.getApplyId());
    }

    @Transactional
    public void cancel(Long orderId) {
        Long providerId = currentUserId();
        Integer roleType = currentUserRoleType();

        if (roleType == null || (roleType != ROLE_CARETAKER && roleType != ROLE_BOTH)) {
            throw new ClientException(BaseErrorCode.CARETAKER_ROLE_REQUIRED_ERROR);
        }

        OrderDO order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_FOUND_ERROR);
        }

        if (order.getStatus() == null || order.getStatus() != ORDER_STATUS_BOUNTY) {
            throw new ClientException(BaseErrorCode.ORDER_NOT_OPEN_ERROR);
        }

        int deletedRows = orderApplicationDao.deleteByOrderIdAndProviderId(orderId, providerId);
        if (deletedRows == 0) {
            throw new ClientException(BaseErrorCode.ORDER_APPLICATION_NOT_FOUND_ERROR);
        }
    }

    private Long currentUserId() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new ClientException(BaseErrorCode.AUTH_TOKEN_INVALID_ERROR);
        }
        return userId;
    }

    private Integer currentUserRoleType() {
        return UserContext.getRoleType();
    }
}
