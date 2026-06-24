package com.eximplatform.payments.dto;

import com.eximplatform.payments.domain.LetterOfCredit;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class LetterOfCreditResponse {

    private UUID id;
    private String lcNumber;
    private UUID orderId;
    private UUID applicantCompanyId;
    private UUID beneficiaryCompanyId;
    private String issuingBank;
    private String advisingBank;
    private BigDecimal amount;
    private String currency;
    private Instant expiryDate;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;

    public static LetterOfCreditResponse from(LetterOfCredit lc) {
        LetterOfCreditResponse out = new LetterOfCreditResponse();
        out.id = lc.getId();
        out.lcNumber = lc.getLcNumber();
        out.orderId = lc.getOrderId();
        out.applicantCompanyId = lc.getApplicantCompanyId();
        out.beneficiaryCompanyId = lc.getBeneficiaryCompanyId();
        out.issuingBank = lc.getIssuingBank();
        out.advisingBank = lc.getAdvisingBank();
        out.amount = lc.getAmount();
        out.currency = lc.getCurrency();
        out.expiryDate = lc.getExpiryDate();
        out.status = lc.getStatus() != null ? lc.getStatus().name() : null;
        out.createdAt = lc.getCreatedAt();
        out.updatedAt = lc.getUpdatedAt();
        return out;
    }

    public UUID getId() { return id; }
    public String getLcNumber() { return lcNumber; }
    public UUID getOrderId() { return orderId; }
    public UUID getApplicantCompanyId() { return applicantCompanyId; }
    public UUID getBeneficiaryCompanyId() { return beneficiaryCompanyId; }
    public String getIssuingBank() { return issuingBank; }
    public String getAdvisingBank() { return advisingBank; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public Instant getExpiryDate() { return expiryDate; }
    public String getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
