package com.eximplatform.payments.domain;

import com.eximplatform.common.domain.BaseEntity;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * A payment term (e.g. NET_30, ADVANCE_100). Seeded reference data; a transaction may reference one
 * by its code. {@code netDays} is the number of days until payment is due (0 = immediate/advance).
 */
@Document(collection = "payment_terms")
public class PaymentTerm extends BaseEntity {

    @Indexed(unique = true)
    private String code;

    private String description;

    private int netDays;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getNetDays() { return netDays; }
    public void setNetDays(int netDays) { this.netDays = netDays; }
}
