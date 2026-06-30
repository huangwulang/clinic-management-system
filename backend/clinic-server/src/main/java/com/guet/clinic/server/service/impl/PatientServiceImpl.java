package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.context.BaseContext;
import com.guet.clinic.common.constant.BusinessStatusConstant;
import com.guet.clinic.pojo.entity.Member;
import com.guet.clinic.pojo.entity.Patient;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.MemberMapper;
import com.guet.clinic.server.mapper.PatientMapper;
import com.guet.clinic.server.mapper.UserAccountMapper;
import com.guet.clinic.server.service.MemberService;
import com.guet.clinic.server.service.PatientService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
public class PatientServiceImpl extends AbstractCrudService<Patient> implements PatientService {
    private static final Map<String, String> LEVEL_NAMES = Map.of(
            "VIP1", "初级会员",
            "VIP2", "高级会员",
            "VIP3", "白银会员",
            "VIP4", "黄金会员",
            "VIP5", "钻石会员"
    );
    private final PatientMapper patientMapper;
    private final UserAccountMapper userAccountMapper;
    private final MemberMapper memberMapper;
    private final MemberService memberService;

    public PatientServiceImpl(PatientMapper patientMapper, UserAccountMapper userAccountMapper,
                              MemberMapper memberMapper, MemberService memberService) {
        this.patientMapper = patientMapper;
        this.userAccountMapper = userAccountMapper;
        this.memberMapper = memberMapper;
        this.memberService = memberService;
    }

    @Override
    protected CrudMapper<Patient> mapper() {
        return patientMapper;
    }

    @Override
    public Patient save(Patient patient) {
        patient.setOperator(currentOperator());
        Patient saved = super.save(patient);
        syncMember(saved);
        return saved;
    }

    @Override
    public Patient update(Long id, Patient patient) {
        Patient updated = super.update(id, patient);
        if (patient.getMemberLevel() != null) {
            String levelCode = normalizeLevelCode(patient.getMemberLevel());
            syncMember(updated, levelCode, LEVEL_NAMES.getOrDefault(levelCode, patient.getMemberLevel()));
        }
        return updated;
    }

    @Override
    @Transactional
    public Patient setMemberLevel(Long id, String levelCode, String memberName, LocalDate expireDate) {
        Patient patient = get(id);
        String normalizedCode = normalizeLevelCode(levelCode);
        String normalizedName = LEVEL_NAMES.get(normalizedCode);
        if (normalizedName == null || !normalizedName.equals(memberName)) {
            throw new com.guet.clinic.common.exception.BusinessException("会员等级与会员名称不匹配");
        }
        patientMapper.updateMemberLevel(id, normalizedCode, expireDate);
        patient.setMemberLevel(normalizedCode);
        patient.setMemberExpireDate(expireDate);
        syncMember(patient, normalizedCode, normalizedName);
        return get(id);
    }

    private String currentOperator() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return null;
        }
        UserAccount user = userAccountMapper.selectById(userId);
        return user == null ? null : user.getName();
    }

    private void syncMember(Patient patient) {
        String levelCode = normalizeLevelCode(patient.getMemberLevel());
        if (levelCode == null || levelCode.isBlank() || "非会员".equals(levelCode)) {
            return;
        }
        syncMember(patient, levelCode, LEVEL_NAMES.getOrDefault(levelCode, patient.getMemberLevel()));
    }

    private void syncMember(Patient patient, String levelCode, String memberName) {
        Member member = memberMapper.selectByPatientId(patient.getId());
        if (member == null) {
            member = new Member();
            member.setPatientId(patient.getId());
            member.setCardNo(patient.getCardNo());
            member.setPatientName(patient.getName());
            member.setPhone(patient.getPhone());
            member.setMemberType(memberName);
            member.setLevelName(levelCode);
            member.setTotalConsume(BigDecimal.ZERO);
            member.setBalance(BigDecimal.ZERO);
            member.setTotalStored(BigDecimal.ZERO);
            member.setPoints(0);
            member.setOpenDate(LocalDate.now());
            member.setExpireDate(patient.getMemberExpireDate());
            member.setStatus(BusinessStatusConstant.ENABLED);
            memberService.save(member);
            return;
        }
        member.setCardNo(patient.getCardNo());
        member.setPatientName(patient.getName());
        member.setPhone(patient.getPhone());
        member.setMemberType(memberName);
        member.setLevelName(levelCode);
        member.setExpireDate(patient.getMemberExpireDate());
        memberService.update(member.getId(), member);
    }

    private String normalizeLevelCode(String level) {
        if (level == null || level.isBlank() || "非会员".equals(level)) {
            return level;
        }
        if (LEVEL_NAMES.containsKey(level)) {
            return level;
        }
        if ("普通会员".equals(level) || "初级会员".equals(level)) return "VIP1";
        if ("高级会员".equals(level)) return "VIP2";
        if ("白银会员".equals(level)) return "VIP3";
        if ("黄金会员".equals(level)) return "VIP4";
        if ("钻石会员".equals(level)) return "VIP5";
        return level;
    }
}
