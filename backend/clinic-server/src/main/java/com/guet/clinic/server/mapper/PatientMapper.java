package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.Patient;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;

@Mapper
public interface PatientMapper extends CrudMapper<Patient> {
    int updateMemberLevel(@Param("id") Long id, @Param("levelCode") String levelCode,
                          @Param("expireDate") LocalDate expireDate);
}
