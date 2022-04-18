package com.nephthys.persistency.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldAttributeDataRestriction implements DataCriterion {

    private String field;
    private DataRestrictionType restrictionType;

    public FieldAttributeDataRestriction(String field, DataRestrictionType restrictionType) {
        this.field = field;
        this.restrictionType = restrictionType;
    }
}
