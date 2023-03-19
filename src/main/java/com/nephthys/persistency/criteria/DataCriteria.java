package com.nephthys.persistency.criteria;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.criterion.MatchMode;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public class DataCriteria<T> implements DataCriterion {

    private Class<T> entityClazz;
    private T entity;
    private Integer firstResult;
    private Integer maxResults;
    private boolean paging = false;
    private Pageable pageable;
    private boolean distinctRoot = false;

    private List<DataCriterion> criterions = new ArrayList<>();
    private List<FieldJoin> joinList = new LinkedList<>();
    private List<Sort.Order> sortOrder = new LinkedList<>();
    private List<Specification<T>> specs = new LinkedList<>();

    private DataCriteria(Class<T> entityClazz) {
        this.entityClazz = entityClazz;
    }

    private DataCriteria(Class<T> entityClazz, Pageable pageable) {
        this.entityClazz = entityClazz;
        if (pageable != null) {
            setPageable(pageable);
            this.paging = true;
        }
    }

    public static <C> DataCriteria<C> create(Class<C> entityClazz) {
        return new DataCriteria<>(entityClazz);
    }

    public static <C> DataCriteria<C> createWithPageable(Class<C> entityClazz, Pageable pageable) {
        return new DataCriteria<>(entityClazz, pageable);
    }

    public DataCriteria<T> add(DataCriterion dataCriterion) {
        this.criterions.add(dataCriterion);
        return this;
    }

    public DataCriteria<T> addSpec(Specification<T> spec) {
        this.specs.add(spec);
        return this;
    }

    //region Json Query Restrictions
    public DataCriteria<T> jsonQueryEq(String field, String paramKey, Object value) {
        if (value != null) {
            add(DataRestrictions.jsonQuery(field, paramKey, value));
        }
        return this;
    }
    //endregion

    //region Restrictions
    public DataCriteria<T> eq(String field, Object value) {
        if (value != null) {
            add(DataRestrictions.eq(field, value));
        }
        return this;
    }

    public DataCriteria<T> eqIgnoreCase(String field, Object value) {
        if (value != null) {
            add(DataRestrictions.eq(field, value).ignoreCase());
        }
        return this;
    }

    public DataCriteria<T> like(String field, Object value, MatchMode matchMode) {
        if (value != null) {
            add(DataRestrictions.like(field, value, matchMode));
        }
        return this;
    }

    public DataCriteria<T> like(String field, Object value, MatchMode matchMode, boolean caseSensitive) {
        if (value != null) {
            add(DataRestrictions.like(field, value, matchMode, caseSensitive));
        }
        return this;
    }

    public DataCriteria<T> like(String field, Object value) {
        if (value != null) {
            add(DataRestrictions.like(field, value));
        }
        return this;
    }

    public DataCriteria<T> lt(String field, Object value) {
        if (value != null) {
            add(DataRestrictions.lt(field, value));
        }
        return this;
    }

    public DataCriteria<T> le(String field, Object value) {
        if (value != null) {
            add(DataRestrictions.le(field, value));
        }
        return this;
    }

    public DataCriteria<T> gt(String field, Object value) {
        if (value != null) {
            add(DataRestrictions.gt(field, value));
        }
        return this;
    }

    public DataCriteria<T> ge(String field, Object value) {
        if (value != null) {
            add(DataRestrictions.ge(field, value));
        }
        return this;
    }

    public DataCriteria<T> ne(String field, Object value) {
        if (value != null) {
            add(DataRestrictions.ne(field, value));
        }
        return this;
    }

    public DataCriteria<T> isNull(String field) {
        add(DataRestrictions.isNull(field));
        return this;
    }

    public DataCriteria<T> in(String field, Object... values) {
        if (ArrayUtils.isNotEmpty(values)) {
            add(DataRestrictions.in(field, values));
        }
        return this;
    }

    public DataCriteria<T> in(String field, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            add(DataRestrictions.in(field, values));
        }
        return this;
    }

    public DataCriteria<T> notIn(String field, Collection<?> values) {
        if (!CollectionUtils.isEmpty(values)) {
            add(DataRestrictions.not(DataRestrictions.in(field, values)));
        }
        return this;
    }

    public DataCriteria<T> isNotNull(String field) {
        add(DataRestrictions.isNotNull(field));
        return this;
    }

    public DataCriteria<T> isEmpty(String field) {
        add(DataRestrictions.isEmpty(field));
        return this;
    }

    public DataCriteria<T> isNotEmpty(String field) {
        add(DataRestrictions.isNotEmpty(field));
        return this;
    }

    public DataCriteria<T> between(String field, Object low, Object high) {
        ge(field, low);
        le(field, high);
        return this;
    }

    public DataCriteria<T> disjunction(DataCriterion... criterions) {
        DataJunction junction = new DataDisjunction();
        add(junction);
        for (DataCriterion dataCriterion : criterions) {
            junction.add(dataCriterion);
        }
        return this;
    }

    public DataCriteria<T> conjunction(DataCriterion... criterions) {
        DataJunction junction = new DataConjunction();
        add(junction);
        for (DataCriterion dataCriterion : criterions) {
            junction.add(dataCriterion);
        }
        return this;
    }

    //endregion

    //region Joins
    public DataCriteria<T> join(String field, String alias) {
        getJoinList().add(new FieldJoin(field, JoinType.LEFT, alias));
        return this;
    }

    public DataCriteria<T> join(String field, JoinType joinType) {
        getJoinList().add(new FieldJoin(field, joinType));
        return this;
    }

    public DataCriteria<T> join(String field, JoinType joinType, String alias) {
        getJoinList().add(new FieldJoin(field, joinType, alias));
        return this;
    }

    public DataCriteria<T> join(String field, JoinType joinType, String alias, boolean hasFetch) {
        getJoinList().add(new FieldJoin(field, joinType, alias, hasFetch));
        return this;
    }

    public DataCriteria<T> fetchJoin(String field) {
        return fetchJoin(field, field);
    }

    public DataCriteria<T> fetchJoin(String field, String alias) {
        return fetchJoin(field, JoinType.LEFT, alias);
    }

    public DataCriteria<T> fetchJoin(String field, JoinType joinType, String alias) {
        return join(field, joinType, alias, true);
    }
    //endregion

    //region Getter-Setter
    public Class<T> getEntityClazz() {
        return entityClazz;
    }

    public void setEntityClazz(Class<T> entityClazz) {
        this.entityClazz = entityClazz;
    }

    public List<FieldJoin> getJoinList() {
        return joinList;
    }

    public void setJoinList(List<FieldJoin> joinList) {
        this.joinList = joinList;
    }

    public List<Sort.Order> getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(List<Sort.Order> sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isDistinctRoot() {
        return distinctRoot;
    }

    public void setDistinctRoot(boolean distinctRoot) {
        this.distinctRoot = distinctRoot;
    }

    public Integer getFirstResult() {
        return firstResult;
    }

    public void setFirstResult(Integer firstResult) {
        this.firstResult = firstResult;
    }

    public Integer getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(Integer maxResults) {
        this.maxResults = maxResults;
    }

    public Pageable getPageable() {
        return pageable;
    }

    private void setPageable(Pageable pageable) {
        this.pageable = pageable;
        setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        setMaxResults(pageable.getPageSize());
    }

    public boolean isPaging() {
        return paging;
    }

    public List<DataCriterion> getCriterions() {
        return criterions;
    }

    public T getEntity() {
        return entity;
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    public List<Specification<T>> getSpecs() {
        return specs;
    }
    //endregion
}
