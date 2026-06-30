package com.guet.clinic.server.service;

import com.guet.clinic.pojo.dto.LoginDTO;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.pojo.vo.LoginVO;

public interface UserAccountService extends CrudService<UserAccount> {
    LoginVO login(LoginDTO loginDTO);

    LoginVO register(LoginDTO loginDTO);

    UserAccount changePassword(Long id, String oldPassword, String newPassword);

    UserAccount enable(Long id);

    UserAccount disable(Long id);

    UserAccount resetPassword(String phone, String newPassword);
}
