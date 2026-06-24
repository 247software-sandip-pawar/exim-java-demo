package com.eximplatform.trust.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

/**
 * A rating one company gives another after a deal. {@code orderId} and the company ids link to other
 * services by bare UUID; {@code score} is 1–5.
 */
@Document(collection = "ratings")
public class Rating extends BaseEntity {

    @Indexed
    private UUID orderId;

    private UUID raterCompanyId;

    @Indexed
    private UUID ratedCompanyId;

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
