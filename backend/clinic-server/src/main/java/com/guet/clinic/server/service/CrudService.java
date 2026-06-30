package com.guet.clinic.server.service;

import com.guet.clinic.common.result.PageResult;
import com.guet.clinic.pojo.entity.BaseEntity;
import java.util.List;

public interface CrudService<T extends BaseEntity> {
    PageResult<T> page(int page, int size, String keyword);
    List<T> list(String keyword);
    long count(String keyword);
    T get(Long id);
    T save(T entity);
    T update(Long id, T entity);
    void delete(Long id);
}
