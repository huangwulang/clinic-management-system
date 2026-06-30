package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.common.context.BaseContext;
import com.guet.clinic.pojo.dto.MemberAdjustDTO;
import com.guet.clinic.pojo.dto.MemberPointsAdjustDTO;
import com.guet.clinic.pojo.entity.Member;
import com.guet.clinic.pojo.entity.MemberTransaction;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.MemberMapper;
import com.guet.clinic.server.mapper.MemberTransactionMapper;
import com.guet.clinic.server.mapper.UserAccountMapper;
import com.guet.clinic.server.service.MemberService;
import com.guet.clinic.server.service.MemberTransactionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class MemberTransactionServiceImpl extends AbstractCrudService<MemberTransaction> implements MemberTransactionService {
    private final MemberTransactionMapper transactionMapper;
    private final MemberMapper memberMapper;
    private final MemberService memberService;
    private final UserAccountMapper userAccountMapper;

    public MemberTransactionServiceImpl(MemberTransactionMapper transactionMapper, MemberMapper memberMapper,
                                        MemberService memberService, UserAccountMapper userAccountMapper) {
        this.transactionMapper = transactionMapper;
        this.memberMapper = memberMapper;
        this.memberService = memberService;
        this.userAccountMapper = userAccountMapper;
    }

    @Override protected CrudMapper<MemberTransaction> mapper() { return transactionMapper; }

    @Override
    public MemberTransaction save(MemberTransaction transaction) {
        if (transaction.getOperator() == null || transaction.getOperator().isBlank()) {
            transaction.setOperator(currentOperator());
        }
        return super.save(transaction);
    }

    @Override @Transactional
    public Member recharge(Long memberId, MemberAdjustDTO dto) {
        dto.setTransactionType(dto.getTransactionType() == null ? "RECHARGE" : dto.getTransactionType());
        return changeBalance(memberId, dto);
    }

    @Override @Transactional
    public Member adjustBalance(Long memberId, MemberAdjustDTO dto) {
        dto.setTransactionType(dto.getTransactionType() == null ? "BALANCE_ADJUST" : dto.getTransactionType());
        return changeBalance(memberId, dto);
    }

    private Member changeBalance(Long memberId, MemberAdjustDTO dto) {
        Member member = memberService.get(memberId);
        BigDecimal current = member.getBalance() == null ? BigDecimal.ZERO : member.getBalance();
        BigDecimal next = current.add(dto.getAmount());
        if (next.compareTo(BigDecimal.ZERO) < 0) throw new BusinessException("会员余额不足");
        member.setBalance(next);
        if (dto.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal stored = member.getTotalStored() == null ? BigDecimal.ZERO : member.getTotalStored();
            member.setTotalStored(stored.add(dto.getAmount()));
        }
        member.setUpdatedAt(LocalDateTime.now());
        memberMapper.update(member);
        MemberTransaction tx = transaction(memberId, dto.getTransactionType(), dto.getBusinessNo(), dto.getRemark(), dto.getOperator());
        tx.setAmount(dto.getAmount());
        tx.setBalanceAfter(next);
        save(tx);
        return memberService.get(memberId);
    }

    @Override @Transactional
    public Member adjustPoints(Long memberId, MemberPointsAdjustDTO dto) {
        Member member = memberService.get(memberId);
        int current = member.getPoints() == null ? 0 : member.getPoints();
        int next = current + dto.getPoints();
        if (next < 0) throw new BusinessException("会员积分不足");
        member.setPoints(next);
        member.setUpdatedAt(LocalDateTime.now());
        memberMapper.update(member);
        MemberTransaction tx = transaction(memberId,
                dto.getTransactionType() == null ? "POINTS_ADJUST" : dto.getTransactionType(),
                dto.getBusinessNo(), dto.getRemark(), dto.getOperator());
        tx.setPoints(dto.getPoints());
        tx.setPointsAfter(next);
        save(tx);
        return memberService.get(memberId);
    }

    private MemberTransaction transaction(Long memberId, String type, String businessNo, String remark, String operator) {
        MemberTransaction tx = new MemberTransaction();
        tx.setMemberId(memberId);
        tx.setTransactionType(type);
        tx.setBusinessNo(businessNo);
        tx.setRemark(remark);
        tx.setOperator(operator == null || operator.isBlank() ? currentOperator() : operator);
        return tx;
    }

    private String currentOperator() {
        Long userId = BaseContext.getCurrentId();
        if (userId == null) return null;
        var user = userAccountMapper.selectById(userId);
        return user == null ? null : user.getName();
    }

    @Override public List<MemberTransaction> balanceLogs(Long memberId) {
        return transactionMapper.selectByMember(memberId, "BALANCE");
    }
    @Override public List<MemberTransaction> pointsLogs(Long memberId) {
        return transactionMapper.selectByMember(memberId, "POINTS");
    }
    @Override public List<MemberTransaction> levelLogs(Long memberId) {
        return transactionMapper.selectByMember(memberId, "LEVEL");
    }
}
