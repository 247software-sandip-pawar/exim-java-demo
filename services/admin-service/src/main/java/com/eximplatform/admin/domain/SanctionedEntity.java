package com.eximplatform.admin.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * An entry on the sanctions / denied-party list. Seeded reference data; screening matches a name
 * against this collection. A real implementation would sync an official feed (OFAC, EU, UN).
 */
@Document(collection = "sanctioned_entities")
public class SanctionedEntity extends BaseEntity {

    @Indexed
    private String name;

    private String country;

    private String programme;

    private String reason;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
    public String getProgramme() { return programme; }
    public void setProgramme(String programme) { this.programme = programme; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
