package com.eximplatform.documents.domain;

/** Kinds of trade document the platform can generate for an order, with a short number prefix. */
public enum DocumentType {
    PROFORMA_INVOICE("PI"),
    COMMERCIAL_INVOICE("CI"),
    PACKING_LIST("PL"),
    BILL_OF_LADING("BL"),
    CERTIFICATE_OF_ORIGIN("CO");

    private final String prefix;

    DocumentType(String prefix) {
        this.prefix = prefix;
    }

    public String prefix() {
        return prefix;
    }
}
