package com.incountry.residence.sdk.dto.search.internal;

public abstract class AbstractFilter {
    public static final String OPERATOR_NOT = "$not";
    public static final String OPERATOR_GREATER = "$gt";
    public static final String OPERATOR_GREATER_OR_EQUALS = "$gte";
    public static final String OPERATOR_LESS = "$lt";
    public static final String OPERATOR_LESS_OR_EQUALS = "$lte";

    public abstract Object toTransferObject();
}
