package com.nephthys.persistency.criteria;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.criteria.JoinType;

@Getter
@Setter
public class FieldJoin {

    private String field;
    private JoinType joinType;
    private String alias;
    private boolean hasFetch;

    public FieldJoin() {
    }

    public FieldJoin(String field, JoinType joinType) {
        this.field = field;
        this.joinType = joinType;
    }

    public FieldJoin(String field, JoinType joinType, String alias) {
        this.field = field;
        this.joinType = joinType;
        this.alias = alias;
    }

    public FieldJoin(String field, JoinType joinType, String alias, boolean hasFetch) {
        this.field = field;
        this.joinType = joinType;
        this.alias = alias;
        this.hasFetch = hasFetch;
    }
}
