package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.Department;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DepartmentMapper extends CrudMapper<Department> {
}
