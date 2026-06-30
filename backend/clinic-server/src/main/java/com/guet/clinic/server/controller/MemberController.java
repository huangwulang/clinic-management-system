package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.MemberAdjustDTO;
import com.guet.clinic.pojo.dto.MemberPointsAdjustDTO;
import com.guet.clinic.pojo.entity.Member;
import com.guet.clinic.pojo.entity.MemberTransaction;
import com.guet.clinic.server.service.MemberService;
import com.guet.clinic.server.service.MemberTransactionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/members")
public class MemberController extends CrudController<Member> {
    private final MemberService memberService;
    private final MemberTransactionService transactionService;
    public MemberController(MemberService memberService, MemberTransactionService transactionService) {
        this.memberService = memberService;
        this.transactionService = transactionService;
    }
    @Override protected MemberService service() { return memberService; }
    @PostMapping("/{id}/recharge") public Result<Member> recharge(@PathVariable Long id, @Valid @RequestBody MemberAdjustDTO dto) {
        return Result.success(transactionService.recharge(id, dto));
    }
    @PostMapping("/{id}/balance-adjust") public Result<Member> balance(@PathVariable Long id, @Valid @RequestBody MemberAdjustDTO dto) {
        return Result.success(transactionService.adjustBalance(id, dto));
    }
    @PostMapping("/{id}/points-adjust") public Result<Member> points(@PathVariable Long id, @Valid @RequestBody MemberPointsAdjustDTO dto) {
        return Result.success(transactionService.adjustPoints(id, dto));
    }
    @GetMapping("/{id}/balance-logs") public Result<List<MemberTransaction>> balanceLogs(@PathVariable Long id) {
        return Result.success(transactionService.balanceLogs(id));
    }
    @GetMapping("/{id}/points-logs") public Result<List<MemberTransaction>> pointsLogs(@PathVariable Long id) {
        return Result.success(transactionService.pointsLogs(id));
    }
    @PostMapping("/{id}/level")
    public Result<Member> changeLevel(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Member member = memberService.get(id);
        String oldLevel = member.getLevelName();
        String levelName = String.valueOf(data.getOrDefault("levelName", oldLevel));
        member.setLevelName(levelName);
        member.setMemberType(levelName);
        Object expireDate = data.get("expireDate");
        member.setExpireDate(expireDate == null || String.valueOf(expireDate).isBlank()
                ? null : LocalDate.parse(String.valueOf(expireDate)));
        Member updated = memberService.update(id, member);

        MemberTransaction transaction = new MemberTransaction();
        transaction.setMemberId(id);
        transaction.setTransactionType("LEVEL_CHANGE");
        transaction.setRemark(oldLevel + " -> " + levelName);
        transactionService.save(transaction);
        return Result.success(updated);
    }
    @GetMapping("/{id}/level-logs") public Result<List<MemberTransaction>> levelLogs(@PathVariable Long id) {
        return Result.success(transactionService.levelLogs(id));
    }
}
