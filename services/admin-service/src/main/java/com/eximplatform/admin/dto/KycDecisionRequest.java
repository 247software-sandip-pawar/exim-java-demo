package com.eximplatform.admin.dto;

/** Optional reviewer note accompanying a KYC approve/reject decision. */
public class KycDecisionRequest {

    private String note;

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
