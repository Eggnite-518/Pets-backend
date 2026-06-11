package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.SubmitReviewAppealReqDTO;
import com.example.pets_backend.dto.req.UpdateCaretakerAvailabilityReqDTO;
import com.example.pets_backend.dto.resp.ActiveOrderRespDTO;
import com.example.pets_backend.dto.resp.CaretakerAvailabilityRespDTO;
import com.example.pets_backend.dto.resp.CaretakerConversationRespDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewItemRespDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewPageRespDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewStatsRespDTO;
import com.example.pets_backend.dto.resp.ReviewAppealRespDTO;
import com.example.pets_backend.dto.resp.ReviewAppealEligibilityRespDTO;
import com.example.pets_backend.dto.resp.SubmitReviewAppealRespDTO;
import com.example.pets_backend.dto.resp.CaretakerStatsRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.CaretakerApplicationService;
import com.example.pets_backend.service.CaretakerConversationService;
import com.example.pets_backend.service.CaretakerProfileService;
import com.example.pets_backend.service.ReviewService;
import com.example.pets_backend.dto.resp.MyApplicationRespDTO;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/caretaker/me")
@RequiredArgsConstructor
public class CaretakerMeController {

    private final CaretakerConversationService caretakerConversationService;
    private final CaretakerProfileService caretakerProfileService;
    private final CaretakerApplicationService caretakerApplicationService;
    private final ReviewService reviewService;

    @GetMapping("/applications")
    public Result<List<MyApplicationRespDTO>> listApplications() {
        return Results.success(caretakerApplicationService.listMyApplications());
    }

    @GetMapping("/orders/active")
    public Result<List<ActiveOrderRespDTO>> listActiveOrders() {
        return Results.success(caretakerApplicationService.listMyActiveOrders());
    }

    @GetMapping("/orders/pending-confirmation")
    public Result<List<ActiveOrderRespDTO>> listPendingConfirmationOrders() {
        return Results.success(caretakerApplicationService.listPendingConfirmationOrders());
    }

    @GetMapping("/orders/today-completed")
    public Result<List<ActiveOrderRespDTO>> listTodayCompletedOrders() {
        return Results.success(caretakerApplicationService.listTodayCompletedOrders());
    }

    @GetMapping("/conversations")
    public Result<List<CaretakerConversationRespDTO>> listConversations() {
        return Results.success(caretakerConversationService.listConversations());
    }

    @GetMapping("/reviews")
    public Result<CaretakerReviewPageRespDTO> listReviews(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) Integer reviewStatus,
            @RequestParam(required = false) Boolean lowScoreOnly) {
        return Results.success(reviewService.listCaretakerReviews(page, pageSize, reviewStatus, lowScoreOnly));
    }

    @GetMapping("/reviews/stats")
    public Result<CaretakerReviewStatsRespDTO> getReviewStats() {
        return Results.success(reviewService.getCaretakerReviewStats());
    }

    @GetMapping("/reviews/{reviewId:\\d+}")
    public Result<CaretakerReviewItemRespDTO> getReview(@PathVariable Long reviewId) {
        return Results.success(reviewService.getCaretakerReviewDetail(reviewId));
    }

    @GetMapping("/stats")
    public Result<CaretakerStatsRespDTO> getStats() {
        return Results.success(caretakerProfileService.getMyStats());
    }

    @GetMapping("/availability")
    public Result<CaretakerAvailabilityRespDTO> getAvailability() {
        return Results.success(caretakerProfileService.getMyAvailability());
    }

    @PutMapping("/availability")
    public Result<CaretakerAvailabilityRespDTO> updateAvailability(
            @RequestBody UpdateCaretakerAvailabilityReqDTO reqDTO) {
        return Results.success(caretakerProfileService.updateMyAvailability(reqDTO));
    }

    @GetMapping("/reviews/{reviewId:\\d+}/appeal-eligibility")
    public Result<ReviewAppealEligibilityRespDTO> getAppealEligibility(@PathVariable Long reviewId) {
        return Results.success(reviewService.getAppealEligibility(reviewId));
    }

    @GetMapping("/reviews/{reviewId:\\d+}/appeals/latest")
    public Result<ReviewAppealRespDTO> getLatestAppeal(@PathVariable Long reviewId) {
        return Results.success(reviewService.getLatestAppeal(reviewId));
    }

    @PostMapping("/reviews/{reviewId:\\d+}/appeals")
    public Result<SubmitReviewAppealRespDTO> submitAppeal(@PathVariable Long reviewId,
            @RequestBody SubmitReviewAppealReqDTO reqDTO) {
        return Results.success(reviewService.submitAppeal(reviewId, reqDTO));
    }
}
