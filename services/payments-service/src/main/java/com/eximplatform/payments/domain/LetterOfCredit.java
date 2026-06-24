package com.eximplatform.payments.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A letter of credit (LC): a bank's guarantee of payment to the seller (beneficiary) on behalf of
 * the buyer (applicant). Company ids link to the identity service by bare UUID; {@code orderId} is
 * an optional link to the order it secures.
 */
@Document(collection = "letters_of_credit")
public class LetterOfCredit extends BaseEntity {

    @Indexed(unique = true)
    private String lcNumber;

    private UUID orderId;

    private UUID applicantCompanyId;

    private UUID beneficiaryCompanyId;

    private String issuingBank;

    private String advisingBank;

    private BigDecimal amount;

    private String currency;

    private Instant expiryDate;

    private LcStatus status = LcStatus.DRAFT;

    public String getLcNumber() { return lcNumber; }
    public void setLcNumber(String lcNumber) { this.lcNumber = lcNumber; }
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
    public LcStatus getStatus() { return status; }
    public void setStatus(LcStatus status) { this.status = status; }
}
