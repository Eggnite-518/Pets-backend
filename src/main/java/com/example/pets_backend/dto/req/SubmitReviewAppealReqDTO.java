package com.example.pets_backend.dto.req;

import java.util.List;

public class SubmitReviewAppealReqDTO {
    private String reason;
    private List<String> evidenceUrls;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getEvidenceUrls() {
        return evidenceUrls;
    }

    public void setEvidenceUrls(List<String> evidenceUrls) {
        this.evidenceUrls = evidenceUrls;
    }
}
