package com.eximplatform.identity.dto;

import com.eximplatform.identity.domain.Company;

import java.time.Instant;
import java.util.UUID;

public class CompanyResponse {

    private UUID id;
    private String name;
    private String type;
    private String country;
    private String iecCode;
    private String gstin;
    private boolean verified;
    private Instant createdAt;
    private Instant updatedAt;

    public static CompanyResponse from(Company company) {
        CompanyResponse r = new CompanyResponse();
        r.id = company.getId();
        r.name = company.getName();
        r.type = company.getType() != null ? company.getType().name() : null;
        r.country = company.getCountry();
        r.iecCode = company.getIecCode();
        r.gstin = company.getGstin();
        r.verified = company.isVerified();
        r.createdAt = company.getCreatedAt();
        r.updatedAt = company.getUpdatedAt();
        return r;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getCountry() { return country; }
    public String getIecCode() { return iecCode; }
    public String getGstin() { return gstin; }
    public boolean isVerified() { return verified; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
