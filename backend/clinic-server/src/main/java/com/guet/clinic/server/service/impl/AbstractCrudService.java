package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.BaseEntity;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.service.CrudService;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

public abstract class AbstractCrudService<T extends BaseEntity> implements CrudService<T> {
    protected abstract CrudMapper<T> mapper();

    @Override
    public PageResult<T> page(int page, int size, String keyword) {
        int safePage = Math.max(page, 1);
        int safeSize = size > 0 ? Math.min(size, 200) : 10;
        int offset = (safePage - 1) * safeSize;
        return PageResult.of(mapper().selectPage(offset, safeSize, keyword), mapper().count(keyword), safePage, safeSize);
    }

    @Override
    public List<T> list(String keyword) { return mapper().selectPage(0, 200, keyword); }

    @Override
    public long count(String keyword) { return mapper().count(keyword); }

    @Override
    public T get(Long id) {
        T entity = mapper().selectById(id);
        if (entity == null) throw new BusinessException("Data not found");
        return entity;
    }

    @Override
    @Transactional
    public T save(T entity) {
        LocalDateTime now = LocalDateTime.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        entity.setDeleted(false);
        mapper().insert(entity);
        return entity;
    }

    @Override
    @Transactional
    public T update(Long id, T entity) {
        get(id);
        entity.setId(id);
        entity.setUpdatedAt(LocalDateTime.now());
        mapper().update(entity);
        return get(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        get(id);
        mapper().softDelete(id);
    }
}
