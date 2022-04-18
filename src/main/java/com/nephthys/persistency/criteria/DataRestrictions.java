package com.nephthys.persistency.criteria;

import org.hibernate.criterion.MatchMode;

import java.util.Collection;

public class DataRestrictions {

    private DataRestrictions() {
    }

    public static SimpleDataRestriction eq(String field, Object value) {
        return new SimpleDataRestriction(field, DataRestrictionType.EQUALS, value);
    }

    public static SimpleDataRestriction like(String field, Object value, MatchMode matchMode) {
        return like(field, value, matchMode, false);
    }

    public static SimpleDataRestriction like(String field, Object value, MatchMode matchMode, boolean caseSensitive) {
        DataRestrictionType restrictionType = DataRestrictionType.LIKE_EXACT;
        switch (matchMode) {
            case ANYWHERE:
                restrictionType = caseSensitive ? DataRestrictionType.LIKE_ANYWHERE_CASE_SENSITIVE : DataRestrictionType.LIKE_ANYWHERE;
                break;
            case END:
                restrictionType = caseSensitive ? DataRestrictionType.LIKE_END_CASE_SENSITIVE : DataRestrictionType.LIKE_END;
                break;
            case EXACT:
                restrictionType = caseSensitive ? DataRestrictionType.LIKE_EXACT_CASE_SENSITIVE : DataRestrictionType.LIKE_EXACT;
                break;
            case START:
                restrictionType = caseSensitive ? DataRestrictionType.LIKE_START_CASE_SENSITIVE : DataRestrictionType.LIKE_START;
                break;
        }
        return new SimpleDataRestriction(field, restrictionType, value);
    }

    public static SimpleDataRestriction like(String field, Object value) {
        return like(field, value, MatchMode.ANYWHERE);
    }

    public static SimpleDataRestriction lt(String field, Object value) {
        return new SimpleDataRestriction(field, DataRestrictionType.LESS_THAN, value);
    }

    public static SimpleDataRestriction le(String field, Object value) {
        return new SimpleDataRestriction(field, DataRestrictionType.LESS_THAN_OR_EQUALS_TO, value);
    }

    public static SimpleDataRestriction gt(String field, Object value) {
        return new SimpleDataRestriction(field, DataRestrictionType.GREATER_THAN, value);
    }

    public static SimpleDataRestriction ge(String field, Object value) {
        return new SimpleDataRestriction(field, DataRestrictionType.GREATER_THAN_OR_EQUALS_TO, value);
    }

    public static SimpleDataRestriction ne(String field, Object value) {
        return new SimpleDataRestriction(field, DataRestrictionType.NOT_EQUALS, value);
    }

    public static FieldAttributeDataRestriction isNull(String field) {
        return new FieldAttributeDataRestriction(field, DataRestrictionType.IS_NULL);
    }

    public static SimpleDataRestriction in(String field, Object[] value) {
        return new SimpleDataRestriction(field, DataRestrictionType.IN, value);
    }

    public static SimpleDataRestriction in(String field, Collection<?> value) {
        return new SimpleDataRestriction(field, DataRestrictionType.IN, value);
    }

    public static FieldAttributeDataRestriction isNotNull(String field) {
        return new FieldAttributeDataRestriction(field, DataRestrictionType.IS_NOT_NULL);
    }

    public static FieldAttributeDataRestriction isEmpty(String field) {
        return new FieldAttributeDataRestriction(field, DataRestrictionType.IS_EMPTY);
    }

    public static FieldAttributeDataRestriction isNotEmpty(String field) {
        return new FieldAttributeDataRestriction(field, DataRestrictionType.IS_NOT_EMPTY);
    }

    public static RangeDataRestriction between(String field, Object low, Object high) {
        return new RangeDataRestriction(field, low, high, DataRestrictionType.BETWEEN);
    }

    public static DataCriterion not(DataCriterion dataCriterion) {
        return new DataNotExpression(dataCriterion);
    }

    public static JsonDataRestriction jsonQuery(String field, String paramKey, Object value) {
        return new JsonDataRestriction(field, value, DataRestrictionType.EQUALS, paramKey);
    }

}
