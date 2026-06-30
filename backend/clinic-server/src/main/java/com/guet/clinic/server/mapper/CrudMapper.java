package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.BaseEntity;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface CrudMapper<T extends BaseEntity> {
    List<T> selectPage(@Param("offset") int offset, @Param("size") int size, @Param("keyword") String keyword);

    long count(@Param("keyword") String keyword);

    T selectById(@Param("id") Long id);

    int insert(T entity);

    int update(T entity);

    int softDelete(@Param("id") Long id);
}
