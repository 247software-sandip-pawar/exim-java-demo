package com.eximplatform.payments.dto;

import com.eximplatform.payments.domain.LcStatus;
import jakarta.validation.constraints.NotNull;

/** Advance a letter of credit to a new status (validated against the allowed transitions). */
public class LcStatusRequest {

    @NotNull
    private LcStatus status;

    public LcStatus getStatus() { return status; }
    public void setStatus(LcStatus status) { this.status = status; }
}
