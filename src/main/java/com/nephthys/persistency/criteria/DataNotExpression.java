package com.nephthys.persistency.criteria;

import lombok.Getter;

@Getter
public class DataNotExpression implements DataCriterion {

    private final DataCriterion dataCriterion;

    public DataNotExpression(DataCriterion dataCriterion) {
        this.dataCriterion = dataCriterion;
    }

}
