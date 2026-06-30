package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.constant.BusinessStatusConstant;
import com.guet.clinic.common.context.BaseContext;
import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.pojo.entity.Registration;
import com.guet.clinic.pojo.entity.Staff;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.RegistrationMapper;
import com.guet.clinic.server.mapper.UserAccountMapper;
import com.guet.clinic.server.service.RegistrationService;
import com.guet.clinic.server.service.StaffService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class RegistrationServiceImpl extends AbstractCrudService<Registration> implements RegistrationService {
    private final RegistrationMapper registrationMapper;
    private final UserAccountMapper userAccountMapper;
    private final StaffService staffService;

    public RegistrationServiceImpl(RegistrationMapper registrationMapper, UserAccountMapper userAccountMapper,
                                   StaffService staffService) {
        this.registrationMapper = registrationMapper;
        this.userAccountMapper = userAccountMapper;
        this.staffService = staffService;
    }

    @Override
    protected CrudMapper<Registration> mapper() {
        return registrationMapper;
    }

    @Override
    public Registration save(Registration registration) {
        registration.setRegistrationNo(nextRegistrationNo());
        registration.setOperator(currentOperator());
        if (registration.getDoctorId() == null && registration.getDoctorName() != null) {
            registration.setDoctorId(staffService.list(registration.getDoctorName()).stream()
                    .filter(staff -> registration.getDoctorName().equals(staff.getName()))
                    .map(Staff::getId)
                    .findFirst()
                    .orElse(null));
        }
        if (registration.getDoctorId() == null) {
            throw new BusinessException("请选择有效接诊医生");
        }
        if (registration.getStatus() == null || registration.getStatus().isBlank()) {
            registration.setStatus(BusinessStatusConstant.PENDING);
        }
        return super.save(registration);
    }

    private String nextRegistrationNo() {
        for (int attempt = 0; attempt < 10; attempt++) {
            String value = String.valueOf(ThreadLocalRandom.current()
                    .nextLong(100_000_000_000L, 1_000_000_000_000L));
            if (registrationMapper.selectByRegistrationNo(value) == null) {
                return value;
            }
        }
        throw new IllegalStateException("Unable to generate registration number");
    }

    private String currentOperator() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) {
            return null;
        }
        UserAccount user = userAccountMapper.selectById(userId);
        return user == null ? null : user.getName();
    }

    @Override
    @Transactional
    public Registration cancel(Long id, String remark) {
        Registration registration = get(id);
        registration.setStatus(BusinessStatusConstant.CANCELLED);
        registration.setRemark(remark == null ? registration.getRemark() : remark);
        registration.setUpdatedAt(LocalDateTime.now());
        registrationMapper.update(registration);
        return get(id);
    }

    @Override
    @Transactional
    public Registration start(Long id) {
        Registration registration = get(id);
        registration.setStatus(BusinessStatusConstant.PROCESSING);
        registration.setUpdatedAt(LocalDateTime.now());
        registrationMapper.update(registration);
        return get(id);
    }

    @Override
    @Transactional
    public Registration complete(Long id) {
        Registration registration = get(id);
        registration.setStatus(BusinessStatusConstant.DONE);
        registration.setUpdatedAt(LocalDateTime.now());
        registrationMapper.update(registration);
        return get(id);
    }
}
