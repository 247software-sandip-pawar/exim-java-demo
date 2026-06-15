package com.eximplatform.identity.dto;

import com.eximplatform.identity.domain.CompanyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Create/update payload for a company. {@code verified} is optional — it defaults to its
 * current value (or {@code false} on create) when omitted.
 */
public class CompanyRequest {

    @NotBlank
    private String name;

    @NotNull
    private CompanyType type;

    private String country;
    private String iecCode;
    private String gstin;
    private Boolean verified;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public CompanyType getType() { return type; }
    public void setType(CompanyType type) { this.type = type; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getIecCode() { return iecCode; }
    public void setIecCode(String iecCode) { this.iecCode = iecCode; }
    public String getGstin() { return gstin; }
    public void setGstin(String gstin) { this.gstin = gstin; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
}
