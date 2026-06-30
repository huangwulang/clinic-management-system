package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.FamilyMember;
import com.guet.clinic.server.service.FamilyMemberService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/family-members")
public class FamilyMemberController extends CrudController<FamilyMember> {
    @Autowired
    private FamilyMemberService familyMemberService;

    @Override
    protected FamilyMemberService service() {
        return familyMemberService;
    }
}
