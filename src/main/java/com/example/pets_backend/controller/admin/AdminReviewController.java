package com.example.pets_backend.controller.admin;

import com.example.pets_backend.dto.req.UpdateReviewAppealStatusReqDTO;
import com.example.pets_backend.dto.req.UpdateReviewStatusReqDTO;
import com.example.pets_backend.dto.resp.AdminReviewAppealPageRespDTO;
import com.example.pets_backend.dto.resp.ReviewAppealRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.AdminAccessService;
import com.example.pets_backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewService reviewService;
    private final AdminAccessService adminAccessService;

    @GetMapping("/appeals")
    public Result<AdminReviewAppealPageRespDTO> listAppeals(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer appealStatus) {
        ensureAdmin();
        return Results.success(reviewService.listAdminAppeals(page, pageSize, appealStatus));
    }

    @GetMapping("/appeals/{appealId}")
    public Result<ReviewAppealRespDTO> getAppealDetail(@PathVariable Long appealId) {
        ensureAdmin();
        return Results.success(reviewService.getAdminAppealDetail(appealId));
    }

    @PostMapping("/{reviewId}/status")
    public Result<Void> updateStatus(@PathVariable Long reviewId, @RequestBody UpdateReviewStatusReqDTO reqDTO) {
        ensureAdmin();
        reviewService.updateReviewStatus(reviewId, reqDTO);
        return Results.success();
    }

    @PostMapping("/appeals/{appealId}/status")
    public Result<Void> updateAppealStatus(@PathVariable Long appealId,
            @RequestBody UpdateReviewAppealStatusReqDTO reqDTO) {
        ensureAdmin();
        reviewService.updateAppealStatus(appealId, reqDTO);
        return Results.success();
    }

    private void ensureAdmin() {
        adminAccessService.ensureAdmin();
    }
}
