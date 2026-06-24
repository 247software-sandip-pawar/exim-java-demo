package com.eximplatform.logistics.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A carrier / 3PL the platform can book shipments with. Seeded reference data (insert-if-empty on
 * startup), referenced by a shipment via its id.
 */
@Document(collection = "logistics_partners")
public class LogisticsPartner extends BaseEntity {

    @Indexed(unique = true)
    private String code;

    private String name;

    private TransportMode mode;

    private boolean active = true;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public TransportMode getMode() { return mode; }
    public void setMode(TransportMode mode) { this.mode = mode; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
