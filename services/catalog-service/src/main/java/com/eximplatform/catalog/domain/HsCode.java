package com.eximplatform.catalog.domain;

import com.eximplatform.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Harmonized System code — the international tariff classification a product is listed under.
 * Reference data, seeded by migration.
 */
@Entity
@Table(name = "hs_codes")
public class HsCode extends BaseEntity {

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false)
    private String description;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
