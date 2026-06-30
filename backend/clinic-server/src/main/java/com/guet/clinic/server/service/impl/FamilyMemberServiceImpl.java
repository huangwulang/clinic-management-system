package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.FamilyMember;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.FamilyMemberMapper;
import com.guet.clinic.server.service.FamilyMemberService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FamilyMemberServiceImpl extends AbstractCrudService<FamilyMember> implements FamilyMemberService {
    private static final String DEFAULT_COMPANY = "无";
    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Override
    protected CrudMapper<FamilyMember> mapper() {
        return familyMemberMapper;
    }

    @Override
    public List<FamilyMember> listByPatientId(Long patientId) {
        return familyMemberMapper.selectByPatientId(patientId);
    }

    @Override
    public FamilyMember save(FamilyMember entity) {
        normalizeCompany(entity);
        return super.save(entity);
    }

    @Override
    public FamilyMember update(Long id, FamilyMember entity) {
        normalizeCompany(entity);
        return super.update(id, entity);
    }

    private void normalizeCompany(FamilyMember entity) {
        if (entity.getCompany() == null || entity.getCompany().isBlank()) {
            entity.setCompany(DEFAULT_COMPANY);
        }
    }
}
