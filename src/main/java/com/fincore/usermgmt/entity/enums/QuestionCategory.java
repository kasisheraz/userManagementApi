package com.fincore.usermgmt.entity.enums;

/**
 * Enumeration representing categories of questionnaire questions
 */
public enum QuestionCategory {
    PERSONAL("Personal information questions"),
    EMPLOYMENT("Employment and income questions"),
    FINANCIAL("Financial background questions");

    private final String description;

    QuestionCategory(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
