package com.nephthys.persistency.criteria;

import lombok.Getter;

@Getter
public class RangeDataRestriction implements DataCriterion {

    private final String field;
    private final Object high;
    private final Object low;
    private final DataRestrictionType restrictionType;

    public RangeDataRestriction(String field, Object low, Object high, DataRestrictionType restrictionType) {
        this.field = field;
        this.high = high;
        this.low = low;
        this.restrictionType = restrictionType;
    }
}
