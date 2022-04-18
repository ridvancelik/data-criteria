package com.nephthys.service;

import com.nephthys.domain.HasId;
import com.nephthys.persistency.criteria.DataCriteria;
import com.nephthys.repository.GenericDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
public class EntityService {

    @Autowired
    private GenericDao genericDao;

    public <T> boolean isExist(Long id, Class<T> clazz) {
        return genericDao.findOne(clazz, "id", id) != null;
    }

    public <T> T findUnique(DataCriteria<T> crt) {
        return genericDao.findOne(crt);
    }

    public <T> List<T> find(DataCriteria<T> crt) {
        return genericDao.find(crt);
    }

    @Transactional
    public void saveOrUpdate(Object entity) {
        genericDao.saveOrUpdate(entity);
    }

    @Transactional
    public void save(Object entity) {
        genericDao.save(entity);
    }

    @Transactional
    public void update(Object entity) {
        genericDao.update(entity);
    }

    public boolean isIdNull(HasId<?> entity) {
        return entity == null || entity.getId() == null;
    }
}
