package com.eximplatform.trust.dto;

import com.eximplatform.trust.domain.DisputeStatus;
import jakarta.validation.constraints.NotNull;

/** Advance a dispute to a new status, optionally recording the resolution. */
public class DisputeStatusRequest {

    @NotNull
    private DisputeStatus status;

    private String resolution;

    public DisputeStatus getStatus() { return status; }
    public void setStatus(DisputeStatus status) { this.status = status; }
    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
