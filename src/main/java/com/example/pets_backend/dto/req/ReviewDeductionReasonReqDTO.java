package com.example.pets_backend.dto.req;

public class ReviewDeductionReasonReqDTO {

    private Integer reasonType;
    private String reasonText;

    public ReviewDeductionReasonReqDTO() {
    }

    public ReviewDeductionReasonReqDTO(Integer reasonType, String reasonText) {
        this.reasonType = reasonType;
        this.reasonText = reasonText;
    }

    public Integer getReasonType() {
        return reasonType;
    }

    public void setReasonType(Integer reasonType) {
        this.reasonType = reasonType;
    }

    public String getReasonText() {
        return reasonText;
    }

    public void setReasonText(String reasonText) {
        this.reasonText = reasonText;
    }
}
