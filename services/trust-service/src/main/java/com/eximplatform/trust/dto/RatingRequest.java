package com.eximplatform.trust.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/** Rate a company for a completed order (score 1–5). */
public class RatingRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    private UUID raterCompanyId;

    @NotNull
    private UUID ratedCompanyId;

    @Min(value = 1, message = "score must be between 1 and 5")
    @Max(value = 5, message = "score must be between 1 and 5")
    private int score;

    private String comment;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getRaterCompanyId() { return raterCompanyId; }
    public void setRaterCompanyId(UUID raterCompanyId) { this.raterCompanyId = raterCompanyId; }
    public UUID getRatedCompanyId() { return ratedCompanyId; }
    public void setRatedCompanyId(UUID ratedCompanyId) { this.ratedCompanyId = ratedCompanyId; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
}
