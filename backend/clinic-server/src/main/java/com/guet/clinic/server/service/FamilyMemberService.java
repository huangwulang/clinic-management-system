package com.guet.clinic.server.service;

import com.guet.clinic.pojo.entity.FamilyMember;

import java.util.List;

public interface FamilyMemberService extends CrudService<FamilyMember> {
    List<FamilyMember> listByPatientId(Long patientId);
}
