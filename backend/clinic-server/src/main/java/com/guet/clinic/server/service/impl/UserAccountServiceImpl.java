package com.guet.clinic.server.service.impl;

import com.guet.clinic.common.exception.BusinessException;
import com.guet.clinic.common.constant.MessageConstant;
import com.guet.clinic.common.exception.LoginFailedException;
import com.guet.clinic.common.utils.PasswordUtil;
import com.guet.clinic.pojo.dto.LoginDTO;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.pojo.vo.LoginVO;
import com.guet.clinic.server.mapper.CrudMapper;
import com.guet.clinic.server.mapper.UserAccountMapper;
import com.guet.clinic.server.service.AuthTokenService;
import com.guet.clinic.server.service.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserAccountServiceImpl extends AbstractCrudService<UserAccount> implements UserAccountService {
    private static final String DEFAULT_USER_PERMISSIONS = "[\"dashboard\"]";

    @Autowired
    private UserAccountMapper userAccountMapper;

    @Autowired
    private AuthTokenService authTokenService;

    @Override
    protected CrudMapper<UserAccount> mapper() {
        return userAccountMapper;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        UserAccount account = userAccountMapper.selectByUsername(loginDTO.getUsername());
        if (account == null || Boolean.FALSE.equals(account.getEnabled())) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        if (!PasswordUtil.matchesClientPassword(loginDTO.getPassword(), account.getPassword())) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        ensureDefaultPermissions(account);
        account.setPassword(null);
        return new LoginVO(authTokenService.createToken(account), account);
    }

    @Override
    @Transactional
    public LoginVO register(LoginDTO loginDTO) {
        UserAccount existed = userAccountMapper.selectByUsername(loginDTO.getUsername());
        if (existed != null) {
            throw new BusinessException("Username already exists");
        }

        UserAccount account = new UserAccount();
        account.setUsername(loginDTO.getUsername());
        account.setPassword(PasswordUtil.encodeClientPassword(loginDTO.getPassword()));
        account.setName(loginDTO.getUsername());
        account.setRoleName("USER");
        account.setEnabled(true);
        save(account);
        ensureDefaultPermissions(account);
        account.setPassword(null);
        return new LoginVO(authTokenService.createToken(account), account);
    }

    private void ensureDefaultPermissions(UserAccount account) {
        if (account == null || account.getPermissions() != null && !account.getPermissions().isBlank()) {
            return;
        }
        if ("USER".equalsIgnoreCase(account.getRoleName())) {
            account.setPermissions(DEFAULT_USER_PERMISSIONS);
        }
    }

    @Override
    @Transactional
    public UserAccount changePassword(Long id, String oldPassword, String newPassword) {
        UserAccount account = get(id);
        if (!PasswordUtil.matchesClientPassword(oldPassword, account.getPassword())) {
            throw new BusinessException("原密码错误");
        }
        account.setPassword(PasswordUtil.encodeClientPassword(newPassword));
        account.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.update(account);
        return get(id);
    }

    @Override
    @Transactional
    public UserAccount enable(Long id) {
        UserAccount account = get(id);
        account.setEnabled(true);
        account.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.update(account);
        return get(id);
    }

    @Override
    @Transactional
    public UserAccount resetPassword(String phone, String newPassword) {
        UserAccount account = userAccountMapper.selectByPhone(phone);
        if (account == null) throw new BusinessException("未找到绑定该手机号的账号");
        account.setPassword(PasswordUtil.encodeClientPassword(newPassword));
        account.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.update(account);
        account.setPassword(null);
        return account;
    }

    @Override
    @Transactional
    public UserAccount disable(Long id) {
        UserAccount account = get(id);
        account.setEnabled(false);
        account.setUpdatedAt(LocalDateTime.now());
        userAccountMapper.update(account);
        return get(id);
    }
}
