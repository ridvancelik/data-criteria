package com.nephthys.persistency.criteria;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DataJunction implements DataCriterion {

    private final DataRelationType relationType;
    private List<DataCriterion> criterions;

    protected DataJunction(DataRelationType relationType) {
        this.relationType = relationType;
        criterions = new ArrayList<>();
    }

    public DataJunction add(DataCriterion criterion) {
        criterions.add(criterion);
        return this;
    }

    public boolean isEmpty() {
        return criterions.isEmpty();
    }
}
