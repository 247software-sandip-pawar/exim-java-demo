package com.eximplatform.admin.dto;

import jakarta.validation.constraints.NotBlank;

/** Screen a name against the sanctions / denied-party list. */
public class SanctionScreenRequest {

    @NotBlank
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
