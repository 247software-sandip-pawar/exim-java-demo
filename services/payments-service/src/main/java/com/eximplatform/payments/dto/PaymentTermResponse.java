package com.eximplatform.payments.dto;

import com.eximplatform.payments.domain.PaymentTerm;

import java.util.UUID;

public record PaymentTermResponse(UUID id, String code, String description, int netDays) {

    public static PaymentTermResponse from(PaymentTerm t) {
        return new PaymentTermResponse(t.getId(), t.getCode(), t.getDescription(), t.getNetDays());
    }
}
