package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.MemberTransaction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface MemberTransactionMapper extends CrudMapper<MemberTransaction> {
    List<MemberTransaction> selectByMember(@Param("memberId") Long memberId, @Param("transactionType") String transactionType);
}
