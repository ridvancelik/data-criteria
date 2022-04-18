package com.nephthys.repository;

import com.nephthys.domain.HasId;
import com.nephthys.domain.IdEntity;
import com.nephthys.persistency.criteria.DataCriteria;
import com.nephthys.persistency.criteria.FieldJoin;
import com.nephthys.utils.CriteriaUtil;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Root;
import java.util.List;

@Repository
public class GenericDao {

    private final EntityManager em;
    private final CriteriaUtil criteriaUtil;

    public GenericDao(EntityManager em, CriteriaUtil criteriaUtil) {
        this.em = em;
        this.criteriaUtil = criteriaUtil;
    }

    public <T> List<T> find(DataCriteria<T> crt) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<T> criteriaQuery = cb.createQuery(crt.getEntityClazz());
        Root<T> root = criteriaQuery.from(crt.getEntityClazz());

        criteriaUtil.buildCriteriaQuery(crt, criteriaQuery, cb, root, false);

        if (crt.isDistinctRoot()) {
            criteriaQuery.select(root).distinct(true);
        }

        TypedQuery<T> query = em.createQuery(criteriaQuery);

        if (crt.getFirstResult() != null) {
            query.setFirstResult(crt.getFirstResult());
        }

        if (crt.getMaxResults() != null) {
            query.setMaxResults(crt.getMaxResults());
        }

        return query.getResultList();
    }

    public <T> Page<T> findPaged(DataCriteria<T> crt) {
        List<T> items = find(crt);
        Page<T> result;
        if (crt.isPaging()) {
            //no fetching needed for countQuery
            for (FieldJoin fieldJoin : crt.getJoinList()) {
                fieldJoin.setHasFetch(false);
            }

            //no sort needed for countQuery
            crt.getSortOrder().clear();

            CriteriaBuilder cb = em.getCriteriaBuilder();

            CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
            Root<T> countRoot = countQuery.from(crt.getEntityClazz());

            criteriaUtil.buildCountQuery(crt, countQuery, cb, countRoot, true);

            Expression<Long> selection;
            if (crt.isDistinctRoot()) {
                selection = cb.countDistinct(countRoot);
            } else {
                selection = cb.count(countRoot);
            }

            countQuery.select(selection);

            TypedQuery<Long> typedQuery = em.createQuery(countQuery);
            Long totalCount = typedQuery.getSingleResult();

            result = new PageImpl<>(items, crt.getPageable(), totalCount);
        } else {
            result = new PageImpl<>(items);
        }

        return result;
    }

    public <T> T findOne(DataCriteria<T> crt) {
        crt.setMaxResults(1);
        List<T> results = find(crt);
        T result = null;
        if (!CollectionUtils.isEmpty(results)) {
            result = results.get(0);
        }
        return result;
    }

    public <T> T findOne(Class<T> clazz, String field, Object value) {
        return findOne(DataCriteria.create(clazz).eq(field, value));
    }

    public <T extends HasId<?>> T get(Class<T> clazz, Long id) {
        return em.find(clazz, id);
    }

    public void save(Object entity) {
        Session session = em.unwrap(Session.class);
        session.save(entity);
    }

    public void update(Object entity) {
        Session session = em.unwrap(Session.class);
        session.update(entity);
    }

    public void saveOrUpdate(Object entity) {
        Session session = em.unwrap(Session.class);
        session.saveOrUpdate(entity);
    }

    public <T extends IdEntity> void softDelete(Class<T> clazz, Long id) {
        String entityName = em.getMetamodel().entity(clazz).getName();
        Query query = em.createQuery("UPDATE " + entityName + " set deleted=true, lastModifiedDate=current_timestamp() where id=:id");
        query.setParameter("id", id);
        query.executeUpdate();
    }

}
