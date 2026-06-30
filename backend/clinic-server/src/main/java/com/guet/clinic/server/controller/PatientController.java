package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.pojo.entity.Consultation;
import com.guet.clinic.pojo.entity.FamilyMember;
import com.guet.clinic.pojo.entity.Patient;
import com.guet.clinic.server.service.ChargeOrderService;
import com.guet.clinic.server.service.ConsultationService;
import com.guet.clinic.server.service.FamilyMemberService;
import com.guet.clinic.server.service.PatientService;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.common.exception.BusinessException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patients")
public class PatientController extends CrudController<Patient> {
    @Autowired
    private PatientService patientService;

    @Autowired
    private FamilyMemberService familyMemberService;

    @Autowired
    private ConsultationService consultationService;

    @Autowired
    private ChargeOrderService chargeOrderService;

    @Override
    protected PatientService service() {
        return patientService;
    }

    @GetMapping("/{id}/family-members")
    public Result<List<FamilyMember>> familyMembers(@PathVariable Long id) {
        return Result.ok(familyMemberService.listByPatientId(id));
    }

    @GetMapping("/{id}/medical-records")
    public Result<List<Consultation>> medicalRecords(@PathVariable Long id) {
        return Result.ok(consultationService.listByPatientId(id));
    }

    @GetMapping("/{id}/charge-orders")
    public Result<List<ChargeOrder>> chargeOrders(@PathVariable Long id) {
        return Result.ok(chargeOrderService.listByPatientId(id));
    }

    @PostMapping("/{id}/member-level")
    public Result<Patient> setMemberLevel(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        String levelCode = String.valueOf(data.getOrDefault("levelCode", "")).trim();
        String memberName = String.valueOf(data.getOrDefault("memberName", "")).trim();
        if (levelCode.isEmpty() || memberName.isEmpty()) {
            throw new BusinessException("会员等级和会员名称不能为空");
        }
        Object expireDateValue = data.get("expireDate");
        LocalDate expireDate = expireDateValue == null || String.valueOf(expireDateValue).isBlank()
                ? null : LocalDate.parse(String.valueOf(expireDateValue));
        return Result.ok(patientService.setMemberLevel(id, levelCode, memberName, expireDate));
    }
}
