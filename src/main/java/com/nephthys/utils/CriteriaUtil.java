package com.nephthys.utils;

import com.nephthys.persistency.criteria.*;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.expression.LiteralExpression;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.*;
import java.util.*;

@Component
public class CriteriaUtil {

    public <T> void buildCriteriaQuery(DataCriteria<T> dataCriteria, CriteriaQuery<T> criteriaQuery, CriteriaBuilder cb, Root<T> root, boolean isCountQuery) {
        Map<String, Join> joins = setJoins(dataCriteria, root, isCountQuery);

        populateCriteria(dataCriteria, criteriaQuery, cb, root, joins);

        setSorts(dataCriteria, criteriaQuery, cb, root, joins);
    }

    public <T> void buildCountQuery(DataCriteria<T> dataCriteria, CriteriaQuery criteriaQuery, CriteriaBuilder cb, Root<T> root, boolean isCountQuery) {
        Map<String, Join> joins = setJoins(dataCriteria, root, isCountQuery);

        populateCriteria(dataCriteria, criteriaQuery, cb, root, joins);
    }

    private <T> Map<String, Join> setJoins(DataCriteria<T> dataCriteria, Root<T> root, boolean isCountQuery) {
        Map<String, Join> aliasJoins = new HashMap<>();
        for (FieldJoin fieldJoin : dataCriteria.getJoinList()) {
            JoinType joinType = JoinType.LEFT;
            if (fieldJoin.getJoinType() == JoinType.INNER) {
                joinType = JoinType.INNER;
            } else if (fieldJoin.getJoinType() == JoinType.RIGHT) {
                joinType = JoinType.RIGHT;
            }

            String[] aliasAndProperty = fieldJoin.getField().split("\\.");
            if (aliasAndProperty.length > 1) {
                String alias = fieldJoin.getAlias();
                Join join = null;
                if (alias != null) {
                    join = aliasJoins.get(alias);
                }

                Join from = aliasJoins.get(aliasAndProperty[0]);
                if (join == null) {
                    if (fieldJoin.isHasFetch() || !isCountQuery) {
                        join = (Join) from.fetch(aliasAndProperty[1], joinType);
                    } else {
                        join = from.join(aliasAndProperty[1], joinType);
                    }
                    if (alias != null) {
                        aliasJoins.put(alias, join);
                    }
                } else {
                    Join subJoin;
                    if (fieldJoin.isHasFetch() || !isCountQuery) {
                        subJoin = (Join) join.fetch(fieldJoin.getField(), joinType);
                    } else {
                        subJoin = join.join(fieldJoin.getField(), joinType);
                    }
                    aliasJoins.put(alias, subJoin);
                }
            } else {
                String alias = fieldJoin.getAlias();
                Join join = null;
                if (alias != null) {
                    join = aliasJoins.get(alias);
                }

                if (join == null) {
                    if (fieldJoin.isHasFetch() || !isCountQuery) {
                        join = (Join) root.fetch(fieldJoin.getField(), joinType);
                    } else {
                        join = root.join(fieldJoin.getField(), joinType);
                    }
                    if (alias != null) {
                        aliasJoins.put(alias, join);
                    }
                } else {
                    Join subJoin;
                    if (fieldJoin.isHasFetch() || !isCountQuery) {
                        subJoin = (Join) join.fetch(fieldJoin.getField(), joinType);
                    } else {
                        subJoin = join.join(fieldJoin.getField(), joinType);
                    }
                    aliasJoins.put(alias, subJoin);
                }
            }
        }
        return aliasJoins;
    }

    private <T> void setSorts(DataCriteria<T> dataCriteria, CriteriaQuery<T> criteriaQuery, CriteriaBuilder cb, Root<T> root, Map<String, Join> joins) {
        List<Order> orderList = new ArrayList<>();

        for (Sort.Order sortOrder : dataCriteria.getSortOrder()) {
            String[] aliasAndProperty = sortOrder.getProperty().split("\\.");
            Expression expression = null;
            expression = getSortExpression(root, joins, sortOrder, aliasAndProperty, expression);
            buildOrder(cb, orderList, sortOrder, expression);
        }

        if (dataCriteria.isPaging() && dataCriteria.getPageable() != null) {
            for (Sort.Order sortOrder : dataCriteria.getPageable().getSort()) {
                String[] aliasAndProperty = sortOrder.getProperty().split("\\.");
                Expression expression = null;
                expression = getSortExpression(root, joins, sortOrder, aliasAndProperty, expression);
                buildOrder(cb, orderList, sortOrder, expression);
            }
        }
        criteriaQuery.orderBy(orderList);
    }

    private void buildOrder(CriteriaBuilder cb, List<Order> orderList, Sort.Order sortOrder, Expression expression) {
        Sort.Direction direction = sortOrder.getDirection();
        if (direction == Sort.Direction.ASC) {
            orderList.add(cb.asc(expression));
        } else if (direction == Sort.Direction.DESC) {
            orderList.add(cb.desc(expression));
        }
    }

    private <T> Expression getSortExpression(Root<T> root, Map<String, Join> joins, Sort.Order sortOrder, String[] aliasAndProperty, Expression expression) {
        if (aliasAndProperty.length > 1) {
            Join join = joins.get(aliasAndProperty[0]);
            if (join != null) {
                expression = join.get(aliasAndProperty[1]);
            }
        } else {
            expression = root.get(sortOrder.getProperty());
        }
        return expression;
    }

    private <T> void populateCriteria(DataCriteria<T> dataCriteria, CriteriaQuery<T> criteriaQuery, CriteriaBuilder cb, Root<T> root, Map<String, Join> joins) {
        List<Predicate> predicates = new ArrayList<>();
        for (DataCriterion dataCriterion : dataCriteria.getCriterions()) {
            Predicate predicate = createPredicate(dataCriterion, criteriaQuery, cb, root, joins);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }
        for (Specification<T> spec : dataCriteria.getSpecs()) {
            Predicate predicate = spec.toPredicate(root, criteriaQuery, cb);
            if (predicate != null) {
                predicates.add(predicate);
            }
        }
        int size = predicates.size();
        criteriaQuery.where(predicates.toArray(new Predicate[size]));
    }

    private <T> Predicate createPredicate(DataCriterion dataCriterion, CriteriaQuery<T> criteriaQuery, CriteriaBuilder cb, Root<T> root, Map<String, Join> joins) {
        Predicate predicate = null;

        if (dataCriterion instanceof SimpleDataRestriction) {
            SimpleDataRestriction restriction = (SimpleDataRestriction) dataCriterion;

            // skip if it s null
            Object value = restriction.getValue();
            String strValue = null;
            boolean needMatch = false;

            if (value != null) {
                if (value instanceof String) {
                    strValue = (String) value;
                    needMatch = !StringUtils.hasLength(strValue);
                } else {
                    needMatch = true;
                }
            }

            if (needMatch) {
                String field = restriction.getField();
                Expression expression = findExpression(root, field, joins);

                if (restriction.isIgnoreCase()) {
                    expression = cb.upper(expression);
                    strValue = strValue.toUpperCase(Locale.ENGLISH);
                }

                switch (restriction.getRestrictionType()) {
                    case EQUALS:
                        predicate = cb.equal(expression, (value instanceof String) ? strValue : value);
                        break;
                    case LIKE_EXACT:
                        predicate = cb.like(expression, strValue);
                        break;
                    case LIKE_ANYWHERE:
                        predicate = cb.like(expression, "%" + strValue + "%");
                        break;
                    case LIKE_END:
                        predicate = cb.like(expression, strValue + "%");
                        break;
                    case LIKE_START:
                        predicate = cb.like(expression, "%" + strValue);
                        break;
                    case GREATER_THAN:
                        predicate = cb.greaterThan(expression, (Comparable) value);
                        break;
                    case GREATER_THAN_OR_EQUALS_TO:
                        predicate = cb.greaterThanOrEqualTo(expression, (Comparable) value);
                        break;
                    case LESS_THAN:
                        predicate = cb.lessThan(expression, (Comparable) value);
                        break;
                    case LESS_THAN_OR_EQUALS_TO:
                        predicate = cb.lessThanOrEqualTo(expression, (Comparable) value);
                        break;
                    case NOT_EQUALS:
                        predicate = cb.notEqual(expression, (value instanceof String) ? strValue : value);
                        break;
                    case IN:
                        if (value instanceof Collection) {
                            predicate = expression.in((Collection<?>) value);
                        } else if (value instanceof Object[]) {
                            predicate = expression.in((Object[]) value);
                        }
                        break;
                    default:
                        break;
                }
            }
        } else if (dataCriterion instanceof RangeDataRestriction) {
            RangeDataRestriction restriction = (RangeDataRestriction) dataCriterion;
            if (restriction.getHigh() != null && restriction.getLow() != null && restriction.getRestrictionType() == DataRestrictionType.BETWEEN) {
                predicate = cb.between(root.get(restriction.getField()), (Comparable) restriction.getLow(), (Comparable) restriction.getHigh());
            }
        } else if (dataCriterion instanceof FieldAttributeDataRestriction) {
            FieldAttributeDataRestriction restriction = (FieldAttributeDataRestriction) dataCriterion;
            Expression expression = findExpression(root, restriction.getField(), joins);
            switch (restriction.getRestrictionType()) {
                case IS_NULL:
                    predicate = cb.isNull(expression);
                    break;
                case IS_NOT_NULL:
                    predicate = cb.isNotNull(expression);
                    break;
                case IS_EMPTY:
                    predicate = cb.isEmpty(expression);
                    break;
                case IS_NOT_EMPTY:
                    predicate = cb.isNotEmpty(expression);
                    break;
            }
        } else if (dataCriterion instanceof DataJunction) {
            DataJunction dataJunction = (DataJunction) dataCriterion;

            if (!dataJunction.isEmpty()) {
                List<Predicate> junctions = new ArrayList<>();

                for (DataCriterion crt : dataJunction.getCriterions()) {
                    Predicate prd = createPredicate(crt, criteriaQuery, cb, root, joins);
                    if (prd != null) {
                        junctions.add(prd);
                    }
                }
                int size = junctions.size();
                if (dataJunction.getRelationType() == DataRelationType.AND) {
                    predicate = cb.and(junctions.toArray(new Predicate[size]));
                } else {
                    predicate = cb.or(junctions.toArray(new Predicate[size]));
                }
            }
        } else if (dataCriterion instanceof DataNotExpression) {
            DataNotExpression dataNotExpression = (DataNotExpression) dataCriterion;

            Predicate prd = createPredicate(dataNotExpression.getDataCriterion(), criteriaQuery, cb, root, joins);

            predicate = cb.not(prd);
        } else if (dataCriterion instanceof JsonDataRestriction) {
            JsonDataRestriction restriction = (JsonDataRestriction) dataCriterion;
            Object value = restriction.getValue();
            String strValue = null;
            Integer intValue = null;
            boolean needMatch = false;

            if (value != null) {
                if (value instanceof String) {
                    strValue = (String) value;
                    needMatch = !StringUtils.hasLength(strValue);
                } else if (value instanceof Integer) {
                    intValue = (Integer) value;
                    needMatch = true;
                }
            }

            if (needMatch) {
                String field = restriction.getField();
                if (restriction.getRestrictionType() == DataRestrictionType.EQUALS) {
                    if (value instanceof String) {
                        predicate = cb.equal(cb.function("JSON_VALUE", String.class,
                                        new HibernateInlineExpression(cb, field),
                                        new HibernateInlineExpression(cb, restriction.getParamKey())),
                                new LiteralExpression<>((CriteriaBuilderImpl) cb, String.class, strValue));
                    } else if (value instanceof Integer) {
                        predicate = cb.equal(cb.function("JSON_VALUE", String.class,
                                        new HibernateInlineExpression(cb, field),
                                        new HibernateInlineExpression(cb, restriction.getParamKey())),
                                new LiteralExpression<>((CriteriaBuilderImpl) cb, Integer.class, intValue));
                    }
                }
            }
        }

        return predicate;
    }

    private <T> Expression findExpression(Root<T> root, String property, Map<String, Join> joins) {
        Expression<Object> expression = null;

        String[] aliasAndProperty = property.split("\\.");
        if (aliasAndProperty.length > 1) {
            String alias = aliasAndProperty[0];
            String prop = aliasAndProperty[1];
            Join join = joins.get(alias);
            if (join != null) {
                expression = join.get(prop);
            }
        } else {
            Join join = joins.get(property);
            if (join != null) {
                expression = join;
            }
        }

        if (expression == null) {
            expression = root.get(property);
        }

        return expression;
    }
}
