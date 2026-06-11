package com.example.pets_backend.dto.req;

import java.util.List;

public class SubmitRatingReqDTO {
    private Integer actionType;
    private Integer score;
    private Integer overallScore;
    private Integer punctualityScore;
    private Integer professionalScore;
    private String comment;
    private List<ReviewDeductionReasonReqDTO> deductionReasons;
    private List<ReviewAttachmentReqDTO> attachments;

    public SubmitRatingReqDTO() {
    }

    public SubmitRatingReqDTO(Integer actionType, Integer score, String comment) {
        this.actionType = actionType;
        this.score = score;
        this.comment = comment;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public Integer getOverallScore() {
        return overallScore;
    }

    public void setOverallScore(Integer overallScore) {
        this.overallScore = overallScore;
    }

    public Integer getPunctualityScore() {
        return punctualityScore;
    }

    public void setPunctualityScore(Integer punctualityScore) {
        this.punctualityScore = punctualityScore;
    }

    public Integer getProfessionalScore() {
        return professionalScore;
    }

    public void setProfessionalScore(Integer professionalScore) {
        this.professionalScore = professionalScore;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<ReviewDeductionReasonReqDTO> getDeductionReasons() {
        return deductionReasons;
    }

    public void setDeductionReasons(List<ReviewDeductionReasonReqDTO> deductionReasons) {
        this.deductionReasons = deductionReasons;
    }

    public List<ReviewAttachmentReqDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<ReviewAttachmentReqDTO> attachments) {
        this.attachments = attachments;
    }
}
