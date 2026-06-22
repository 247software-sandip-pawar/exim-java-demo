package com.eximplatform.catalog.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Harmonized System code — the international tariff classification a product is listed under.
 * Reference data, seeded on startup by {@code HsCodeSeeder}.
 */
@Document(collection = "hs_codes")
public class HsCode extends BaseEntity {

    @Indexed(unique = true)
    private String code;

    private String description;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
