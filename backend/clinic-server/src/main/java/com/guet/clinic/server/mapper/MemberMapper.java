package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper extends CrudMapper<Member> {
    Member selectByPatientId(@Param("patientId") Long patientId);
}
