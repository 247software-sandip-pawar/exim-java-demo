package com.eximplatform.payments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Open a letter of credit securing payment from the applicant (buyer) to the beneficiary (seller). */
public class LetterOfCreditRequest {

    private UUID orderId;

    @NotNull
    private UUID applicantCompanyId;

    @NotNull
    private UUID beneficiaryCompanyId;

    @NotBlank
    private String issuingBank;

    private String advisingBank;

    @NotNull
    private BigDecimal amount;

    @NotBlank
    private String currency;

    private Instant expiryDate;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public UUID getApplicantCompanyId() { return applicantCompanyId; }
    public void setApplicantCompanyId(UUID applicantCompanyId) { this.applicantCompanyId = applicantCompanyId; }
    public UUID getBeneficiaryCompanyId() { return beneficiaryCompanyId; }
    public void setBeneficiaryCompanyId(UUID beneficiaryCompanyId) { this.beneficiaryCompanyId = beneficiaryCompanyId; }
    public String getIssuingBank() { return issuingBank; }
    public void setIssuingBank(String issuingBank) { this.issuingBank = issuingBank; }
    public String getAdvisingBank() { return advisingBank; }
    public void setAdvisingBank(String advisingBank) { this.advisingBank = advisingBank; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    public Instant getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Instant expiryDate) { this.expiryDate = expiryDate; }
}
