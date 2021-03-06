package com.nephthys.persistency.criteria;

public enum DataRestrictionType {
    EQUALS,
    LIKE_END,
    LIKE_START,
    LIKE_ANYWHERE,
    LIKE_EXACT,
    NOT_EQUALS,
    GREATER_THAN,
    LESS_THAN,
    GREATER_THAN_OR_EQUALS_TO,
    LESS_THAN_OR_EQUALS_TO,
    IS_NULL,
    IS_NOT_NULL,
    IS_EMPTY,
    IS_NOT_EMPTY,
    BETWEEN,
    IN,
    LIKE_START_CASE_SENSITIVE,
    LIKE_END_CASE_SENSITIVE,
    LIKE_ANYWHERE_CASE_SENSITIVE,
    LIKE_EXACT_CASE_SENSITIVE,
    NOT_IN,
    NOT,
}
