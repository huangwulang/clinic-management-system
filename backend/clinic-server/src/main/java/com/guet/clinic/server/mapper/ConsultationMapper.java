package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.Consultation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConsultationMapper extends CrudMapper<Consultation> {
    List<Consultation> selectByPatientId(@Param("patientId") Long patientId);
}
