package com.example.pets_backend.dto.req;

public class UpdateReviewStatusReqDTO {
    private Integer reviewStatus;
    private String reason;

    public Integer getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(Integer reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
