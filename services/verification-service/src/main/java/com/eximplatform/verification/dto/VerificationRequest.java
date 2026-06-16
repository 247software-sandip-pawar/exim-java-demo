package com.eximplatform.verification.dto;

import com.eximplatform.verification.domain.VerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Submit payload for a KYC document. {@code fileUrl} is the location of the already-uploaded
 * document (a real multipart upload via a StorageService is a deferred Phase-2 add).
 */
public class VerificationRequest {

    @NotNull
    private VerificationType type;

    @NotBlank
    private String fileUrl;

    public VerificationType getType() { return type; }
    public void setType(VerificationType type) { this.type = type; }
    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
