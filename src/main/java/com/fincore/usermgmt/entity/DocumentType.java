package com.fincore.usermgmt.entity;

/**
 * Enum representing different types of KYC documents.
 * Maps to Document_Type_Description column in KYC_Documents table.
 */
public enum DocumentType {
    CERTIFICATE_OF_INCORPORATION("Certificate of Incorporation"),
    MEMORANDUM_OF_ASSOCIATION("Memorandum of Association"),
    ARTICLES_OF_ASSOCIATION("Articles of Association"),
    PROOF_OF_ADDRESS("Proof of Address"),
    BANK_STATEMENT("Bank Statement"),
    UTILITY_BILL("Utility Bill"),
    ID_DOCUMENT("Identity Document"),
    PASSPORT("Passport"),
    DRIVING_LICENSE("Driving License"),
    HMRC_REGISTRATION("HMRC Registration Document"),
    FCA_AUTHORISATION("FCA Authorisation Letter"),
    ICO_REGISTRATION("ICO Registration Certificate"),
    BUSINESS_LICENSE("Business License"),
    TAX_RETURN("Tax Return"),
    FINANCIAL_STATEMENT("Financial Statement"),
    SHAREHOLDER_REGISTER("Register of Shareholders"),
    DIRECTOR_RESOLUTION("Director Resolution"),
    OTHER("Other Document");

    private final String description;

    DocumentType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
