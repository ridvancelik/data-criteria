package com.nephthys.persistency.criteria;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonDataRestriction implements DataCriterion {

    private String field;
    private Object value;
    private DataRestrictionType restrictionType;
    private String paramKey;

    public JsonDataRestriction(String field, Object value, DataRestrictionType restrictionType, String paramKey) {
        this.field = field;
        this.value = value;
        this.restrictionType = restrictionType;
        this.paramKey = paramKey;
    }
}

