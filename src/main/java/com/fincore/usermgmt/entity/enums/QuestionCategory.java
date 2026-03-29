package com.fincore.usermgmt.entity.enums;

/**
 * Enumeration representing categories of questionnaire questions
 */
public enum QuestionCategory {
    PERSONAL("Personal information questions"),
    OCCUPATION("Occupation and employment questions"),
    INCOME("Income and earnings questions"),
    EMPLOYMENT("Employment and income questions"),
    FINANCIAL("Financial background questions"),
    LEGAL("Legal and compliance questions"),
    OPERATIONAL("Operational questions"),
    COMPLIANCE("Compliance and regulatory questions"),
    REGULATORY("Regulatory requirements questions"),
    OTHER("Other questions");

    private final String description;

    QuestionCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
