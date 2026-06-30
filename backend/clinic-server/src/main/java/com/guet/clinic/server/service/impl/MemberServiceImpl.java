package com.guet.clinic.server.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.pojo.entity.Member;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.MemberMapper;
import com.guet.clinic.server.service.MemberService;
import org.springframework.stereotype.Service;

@Service
public class MemberServiceImpl extends AbstractCrudService<Member> implements MemberService {
    @Autowired
    private MemberMapper memberMapper;

    @Override
    protected CrudMapper<Member> mapper() {
        return memberMapper;
    }
}
