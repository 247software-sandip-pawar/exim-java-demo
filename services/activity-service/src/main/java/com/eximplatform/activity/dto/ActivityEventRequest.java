package com.eximplatform.activity.dto;

import jakarta.validation.constraints.NotBlank;

/** Recorded by the gateway for each authenticated request; the actor comes from the caller's JWT. */
public class ActivityEventRequest {

    @NotBlank
    private String method;

    @NotBlank
    private String path;

    private int status;

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
}
