package com.example.pets_backend.controller.admin;

import com.example.pets_backend.dto.resp.EvidenceChainRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.AdminAccessService;
import com.example.pets_backend.service.EvidenceChainService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final EvidenceChainService evidenceChainService;
    private final AdminAccessService adminAccessService;

    @GetMapping("/{orderId}/evidence")
    public Result<EvidenceChainRespDTO> getEvidence(@PathVariable Long orderId) {
        adminAccessService.ensureAdmin();
        return Results.success(evidenceChainService.getEvidenceChain(orderId));
    }
}
