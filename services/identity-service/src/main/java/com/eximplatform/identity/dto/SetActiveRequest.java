package com.eximplatform.identity.dto;

import jakarta.validation.constraints.NotNull;

/** Activate or deactivate a user / company (moderation). */
public class SetActiveRequest {

    @NotNull
    private Boolean active;

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}
