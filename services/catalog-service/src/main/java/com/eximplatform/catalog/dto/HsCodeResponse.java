package com.eximplatform.catalog.dto;

import com.eximplatform.catalog.domain.HsCode;

import java.util.UUID;

public class HsCodeResponse {

    private UUID id;
    private String code;
    private String description;

    public static HsCodeResponse from(HsCode h) {
        HsCodeResponse r = new HsCodeResponse();
        r.id = h.getId();
        r.code = h.getCode();
        r.description = h.getDescription();
        return r;
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
}
