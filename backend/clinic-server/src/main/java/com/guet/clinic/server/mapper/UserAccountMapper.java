package com.guet.clinic.server.mapper;

import com.guet.clinic.pojo.entity.UserAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserAccountMapper extends CrudMapper<UserAccount> {
    UserAccount selectByUsername(@Param("username") String username);

    UserAccount selectByPhone(@Param("phone") String phone);
}
