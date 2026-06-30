package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.FamilyMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FamilyMemberMapper extends CrudMapper<FamilyMember> {
    List<FamilyMember> selectByPatientId(@Param("patientId") Long patientId);
}
