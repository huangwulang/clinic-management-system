package com.guet.clinic.server.service;

import com.guet.clinic.pojo.dto.MemberAdjustDTO;
import com.guet.clinic.pojo.dto.MemberPointsAdjustDTO;
import com.guet.clinic.pojo.entity.Member;
import com.guet.clinic.pojo.entity.MemberTransaction;
import java.util.List;

public interface MemberTransactionService extends CrudService<MemberTransaction> {
    Member recharge(Long memberId, MemberAdjustDTO dto);
    Member adjustBalance(Long memberId, MemberAdjustDTO dto);
    Member adjustPoints(Long memberId, MemberPointsAdjustDTO dto);
    List<MemberTransaction> balanceLogs(Long memberId);
    List<MemberTransaction> pointsLogs(Long memberId);
    List<MemberTransaction> levelLogs(Long memberId);
}
