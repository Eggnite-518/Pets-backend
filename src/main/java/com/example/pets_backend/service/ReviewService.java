package com.example.pets_backend.service;

import com.example.pets_backend.dto.req.SubmitReviewAppealReqDTO;
import com.example.pets_backend.dto.req.UpdateReviewAppealStatusReqDTO;
import com.example.pets_backend.dto.req.UpdateReviewStatusReqDTO;
import com.example.pets_backend.dto.resp.AdminReviewAppealPageRespDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewItemRespDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewPageRespDTO;
import com.example.pets_backend.dto.resp.CaretakerReviewStatsRespDTO;
import com.example.pets_backend.dto.resp.ReviewAppealRespDTO;
import com.example.pets_backend.dto.resp.ReviewAppealEligibilityRespDTO;
import com.example.pets_backend.dto.resp.SubmitReviewAppealRespDTO;

public interface ReviewService {

    CaretakerReviewPageRespDTO listCaretakerReviews(Integer page, Integer pageSize,
            Integer reviewStatus, Boolean lowScoreOnly);

    CaretakerReviewItemRespDTO getCaretakerReviewDetail(Long reviewId);

    CaretakerReviewStatsRespDTO getCaretakerReviewStats();

    ReviewAppealEligibilityRespDTO getAppealEligibility(Long reviewId);

    ReviewAppealRespDTO getLatestAppeal(Long reviewId);

    SubmitReviewAppealRespDTO submitAppeal(Long reviewId, SubmitReviewAppealReqDTO reqDTO);

    AdminReviewAppealPageRespDTO listAdminAppeals(Integer page, Integer pageSize, Integer appealStatus);

    ReviewAppealRespDTO getAdminAppealDetail(Long appealId);

    void updateReviewStatus(Long reviewId, UpdateReviewStatusReqDTO reqDTO);

    void updateAppealStatus(Long appealId, UpdateReviewAppealStatusReqDTO reqDTO);
}
