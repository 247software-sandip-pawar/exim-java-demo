package com.eximplatform.identity.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "companies")
public class Company extends BaseEntity {

    private String name;

    private CompanyType type;

    private String country;
    private String iecCode;
    private String gstin;

    private boolean verified = false;

    /** Whether the company is active. A suspended company's members are blocked at login. */
    private boolean active = true;

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
    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
