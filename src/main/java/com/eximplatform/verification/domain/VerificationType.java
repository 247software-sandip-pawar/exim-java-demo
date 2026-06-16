package com.eximplatform.verification.domain;

/**
 * Kind of KYC document a company submits for verification.
 * IEC = Importer Exporter Code, GST = GST registration, RCMC = Registration-cum-Membership
 * Certificate, BANK = bank account proof.
 */
public enum VerificationType {
    IEC, GST, RCMC, BANK
}
