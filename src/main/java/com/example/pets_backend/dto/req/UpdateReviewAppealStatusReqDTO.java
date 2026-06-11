package com.example.pets_backend.dto.req;

public class UpdateReviewAppealStatusReqDTO {
    private Integer appealStatus;
    private String adminMemo;

    public Integer getAppealStatus() {
        return appealStatus;
    }

    public void setAppealStatus(Integer appealStatus) {
        this.appealStatus = appealStatus;
    }

    public String getAdminMemo() {
        return adminMemo;
    }

    public void setAdminMemo(String adminMemo) {
        this.adminMemo = adminMemo;
    }
}
