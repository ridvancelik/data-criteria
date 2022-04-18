package com.nephthys.persistency.criteria;

import lombok.Getter;

@Getter
public class SimpleDataRestriction implements DataCriterion {

    private final String field;
    private final Object value;
    private boolean ignoreCase;
    private final DataRestrictionType restrictionType;

    public SimpleDataRestriction(String field, DataRestrictionType restrictionType, Object value) {
        this.field = field;
        this.value = value;
        this.restrictionType = restrictionType;
    }

    public SimpleDataRestriction ignoreCase() {
        ignoreCase = true;
        return this;
    }
}
