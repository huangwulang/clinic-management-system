package com.guet.clinic.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.PasswordUpdateDTO;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.server.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-accounts")
public class UserAccountController extends CrudController<UserAccount> {
    @Autowired
    private UserAccountService userAccountService;

    @Override
    protected UserAccountService service() {
        return userAccountService;
    }

    @PutMapping("/{id}/password")
    public Result<UserAccount> changePassword(@PathVariable Long id, @Valid @RequestBody PasswordUpdateDTO passwordUpdateDTO) {
        return Result.success(userAccountService.changePassword(id, passwordUpdateDTO.getOldPassword(), passwordUpdateDTO.getNewPassword()));
    }

    @PostMapping("/{id}/enable")
    public Result<UserAccount> enable(@PathVariable Long id) {
        return Result.success(userAccountService.enable(id));
    }

    @PostMapping("/{id}/disable")
    public Result<UserAccount> disable(@PathVariable Long id) {
        return Result.success(userAccountService.disable(id));
    }
}
