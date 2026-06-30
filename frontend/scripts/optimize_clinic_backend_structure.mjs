import fs from "node:fs";
import path from "node:path";

const root = "D:\\idea_code\\clinic-menagement-system";
const commonJava = path.join(root, "clinic-common", "src", "main", "java", "com", "guet", "clinic", "common");
const pojoJava = path.join(root, "clinic-pojo", "src", "main", "java", "com", "guet", "clinic", "pojo");
const serverJava = path.join(root, "clinic-server", "src", "main", "java");

function mkdir(dir) {
  fs.mkdirSync(dir, { recursive: true });
}

function write(file, content) {
  mkdir(path.dirname(file));
  fs.writeFileSync(file, content.replace(/\n/g, "\r\n"), "utf8");
}

function remove(file) {
  if (fs.existsSync(file)) fs.rmSync(file);
}

function walk(dir, matcher = () => true) {
  const out = [];
  if (!fs.existsSync(dir)) return out;
  for (const entry of fs.readdirSync(dir, { withFileTypes: true })) {
    const full = path.join(dir, entry.name);
    if (entry.isDirectory()) out.push(...walk(full, matcher));
    else if (matcher(full)) out.push(full);
  }
  return out;
}

function replaceInJava(search, replacement) {
  for (const file of walk(root, (item) => item.endsWith(".java"))) {
    const before = fs.readFileSync(file, "utf8");
    const after = before.split(search).join(replacement);
    if (before !== after) fs.writeFileSync(file, after, "utf8");
  }
}

mkdir(path.join(commonJava, "result"));
mkdir(path.join(commonJava, "constant"));
mkdir(path.join(commonJava, "exception"));
mkdir(path.join(commonJava, "context"));
mkdir(path.join(pojoJava, "dto"));
mkdir(path.join(pojoJava, "entity"));
mkdir(path.join(pojoJava, "vo"));

write(path.join(commonJava, "result", "Result.java"), `package com.guet.clinic.common.result;

import com.guet.clinic.common.constant.StatusConstant;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = StatusConstant.SUCCESS;
        result.message = "success";
        result.data = data;
        result.timestamp = LocalDateTime.now();
        return result;
    }

    public static <T> Result<T> ok(T data) {
        return success(data);
    }

    public static Result<Void> ok() {
        return success(null);
    }

    public static Result<Void> error(String message) {
        return fail(StatusConstant.ERROR, message);
    }

    public static Result<Void> fail(Integer code, String message) {
        Result<Void> result = new Result<>();
        result.code = code;
        result.message = message;
        result.timestamp = LocalDateTime.now();
        return result;
    }
}
`);

write(path.join(commonJava, "result", "PageResult.java"), `package com.guet.clinic.common.result;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private List<T> records;
    private long total;
    private int page;
    private int size;

    public static <T> PageResult<T> of(List<T> records, long total, int page, int size) {
        return new PageResult<>(records, total, page, size);
    }
}
`);

write(path.join(commonJava, "exception", "BaseException.java"), `package com.guet.clinic.common.exception;

public class BaseException extends RuntimeException {
    public BaseException() {
    }

    public BaseException(String message) {
        super(message);
    }
}
`);

write(path.join(commonJava, "exception", "BusinessException.java"), `package com.guet.clinic.common.exception;

public class BusinessException extends BaseException {
    public BusinessException(String message) {
        super(message);
    }
}
`);

write(path.join(commonJava, "exception", "LoginFailedException.java"), `package com.guet.clinic.common.exception;

public class LoginFailedException extends BaseException {
    public LoginFailedException(String message) {
        super(message);
    }
}
`);

write(path.join(commonJava, "constant", "StatusConstant.java"), `package com.guet.clinic.common.constant;

public class StatusConstant {
    public static final int SUCCESS = 200;
    public static final int ERROR = 500;
    public static final String ENABLE = "启用";
    public static final String DISABLE = "停用";

    private StatusConstant() {
    }
}
`);

write(path.join(commonJava, "constant", "MessageConstant.java"), `package com.guet.clinic.common.constant;

public class MessageConstant {
    public static final String LOGIN_FAILED = "用户名或密码错误";
    public static final String DATA_NOT_FOUND = "数据不存在";
    public static final String VALIDATION_FAILED = "参数校验失败";
    public static final String UNKNOWN_ERROR = "系统异常";

    private MessageConstant() {
    }
}
`);

write(path.join(commonJava, "constant", "JwtClaimsConstant.java"), `package com.guet.clinic.common.constant;

public class JwtClaimsConstant {
    public static final String USER_ID = "userId";
    public static final String USERNAME = "username";
    public static final String NAME = "name";
    public static final String ROLE = "role";

    private JwtClaimsConstant() {
    }
}
`);

write(path.join(commonJava, "constant", "SystemConstant.java"), `package com.guet.clinic.common.constant;

public class SystemConstant {
    public static final String DEFAULT_OPERATOR = "系统";
    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_PAGE_SIZE = 10;

    private SystemConstant() {
    }
}
`);

write(path.join(commonJava, "context", "BaseContext.java"), `package com.guet.clinic.common.context;

public class BaseContext {
    private static final ThreadLocal<Long> CURRENT_ID = new ThreadLocal<>();

    public static void setCurrentId(Long id) {
        CURRENT_ID.set(id);
    }

    public static Long getCurrentId() {
        return CURRENT_ID.get();
    }

    public static void removeCurrentId() {
        CURRENT_ID.remove();
    }

    private BaseContext() {
    }
}
`);

const oldBase = path.join(pojoJava, "BaseEntity.java");
const newBase = path.join(pojoJava, "entity", "BaseEntity.java");
if (fs.existsSync(oldBase)) {
  const content = fs.readFileSync(oldBase, "utf8").replace("package com.guet.clinic.pojo;", "package com.guet.clinic.pojo.entity;");
  fs.writeFileSync(newBase, content, "utf8");
  remove(oldBase);
}

remove(path.join(commonJava, "ApiResponse.java"));
remove(path.join(commonJava, "PageResponse.java"));
remove(path.join(commonJava, "BusinessException.java"));

replaceInJava("import com.guet.clinic.common.ApiResponse;", "import com.guet.clinic.common.result.Result;");
replaceInJava("import com.guet.clinic.common.PageResponse;", "import com.guet.clinic.common.result.PageResult;");
replaceInJava("import com.guet.clinic.common.BusinessException;", "import com.guet.clinic.common.exception.BusinessException;");
replaceInJava("import com.guet.clinic.pojo.BaseEntity;", "import com.guet.clinic.pojo.entity.BaseEntity;");
replaceInJava("ApiResponse<", "Result<");
replaceInJava("ApiResponse.", "Result.");
replaceInJava("PageResponse<", "PageResult<");
replaceInJava("PageResponse.of", "PageResult.of");

for (const file of walk(path.join(pojoJava, "entity"), (item) => item.endsWith(".java"))) {
  const before = fs.readFileSync(file, "utf8");
  const after = before.replace(/^import com\.guet\.clinic\.pojo\.entity\.BaseEntity;\r?\n/m, "");
  if (before !== after) fs.writeFileSync(file, after, "utf8");
}

write(path.join(pojoJava, "dto", "LoginDTO.java"), `package com.guet.clinic.pojo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginDTO {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
`);

write(path.join(pojoJava, "dto", "PageQueryDTO.java"), `package com.guet.clinic.pojo.dto;

import lombok.Data;

@Data
public class PageQueryDTO {
    private int page = 1;
    private int size = 10;
    private String keyword;
}
`);

write(path.join(pojoJava, "dto", "ChargePayDTO.java"), `package com.guet.clinic.pojo.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ChargePayDTO {
    private BigDecimal paidAmount;
    private String payMethod;
    private String cashier;
}
`);

write(path.join(pojoJava, "dto", "ChargeRefundDTO.java"), `package com.guet.clinic.pojo.dto;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class ChargeRefundDTO {
    private BigDecimal refundAmount;
    private String refundMethod;
}
`);

write(path.join(pojoJava, "vo", "LoginVO.java"), `package com.guet.clinic.pojo.vo;

import com.guet.clinic.pojo.entity.UserAccount;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    private String token;
    private UserAccount user;
}
`);

write(path.join(pojoJava, "vo", "DashboardVO.java"), `package com.guet.clinic.pojo.vo;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {
    private long patientCount;
    private long registrationCount;
    private long drugCount;
    private long memberCount;
    private BigDecimal income;
}
`);

const entityFiles = walk(path.join(pojoJava, "entity"), (item) => item.endsWith(".java") && path.basename(item) !== "BaseEntity.java");
for (const file of entityFiles) {
  const model = path.basename(file, ".java");
  write(path.join(pojoJava, "dto", `${model}DTO.java`), `package com.guet.clinic.pojo.dto;

import com.guet.clinic.pojo.entity.${model};
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ${model}DTO extends ${model} {
}
`);
  write(path.join(pojoJava, "vo", `${model}VO.java`), `package com.guet.clinic.pojo.vo;

import com.guet.clinic.pojo.entity.${model};
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class ${model}VO extends ${model} {
}
`);
}

write(path.join(serverJava, "com", "guet", "clinic", "server", "controller", "AuthController.java"), `package com.guet.clinic.server.controller;

import com.guet.clinic.common.constant.MessageConstant;
import com.guet.clinic.common.exception.LoginFailedException;
import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.LoginDTO;
import com.guet.clinic.pojo.entity.UserAccount;
import com.guet.clinic.pojo.vo.LoginVO;
import com.guet.clinic.server.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final UserAccountService userAccountService;

    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        UserAccount user = userAccountService.list(loginDTO.getUsername()).stream()
                .filter(item -> loginDTO.getUsername().equals(item.getUsername()))
                .findFirst()
                .orElseThrow(() -> new LoginFailedException(MessageConstant.LOGIN_FAILED));
        if (!loginDTO.getPassword().equals(user.getPassword())) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        return Result.success(new LoginVO(UUID.randomUUID().toString(), user));
    }
}
`);

write(path.join(serverJava, "com", "guet", "clinic", "server", "controller", "ChargeOrderController.java"), `package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.dto.ChargePayDTO;
import com.guet.clinic.pojo.dto.ChargeRefundDTO;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.server.service.ChargeOrderService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/charge-orders")
public class ChargeOrderController extends CrudController<ChargeOrder> {
    private final ChargeOrderService service;

    public ChargeOrderController(ChargeOrderService service) {
        this.service = service;
    }

    @Override
    protected ChargeOrderService service() {
        return service;
    }

    @PostMapping("/{id}/pay")
    public Result<ChargeOrder> pay(@PathVariable Long id, @RequestBody ChargePayDTO chargePayDTO) {
        return Result.success(service.pay(id, chargePayDTO.getPaidAmount(), chargePayDTO.getPayMethod(), chargePayDTO.getCashier()));
    }

    @PostMapping("/{id}/refund")
    public Result<ChargeOrder> refund(@PathVariable Long id, @RequestBody ChargeRefundDTO chargeRefundDTO) {
        return Result.success(service.refund(id, chargeRefundDTO.getRefundAmount(), chargeRefundDTO.getRefundMethod()));
    }
}
`);

write(path.join(serverJava, "com", "guet", "clinic", "server", "controller", "StatisticsController.java"), `package com.guet.clinic.server.controller;

import com.guet.clinic.common.result.Result;
import com.guet.clinic.pojo.entity.ChargeOrder;
import com.guet.clinic.pojo.vo.DashboardVO;
import com.guet.clinic.server.service.ChargeOrderService;
import com.guet.clinic.server.service.DrugService;
import com.guet.clinic.server.service.MemberService;
import com.guet.clinic.server.service.PatientService;
import com.guet.clinic.server.service.RegistrationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    private final PatientService patientService;
    private final RegistrationService registrationService;
    private final ChargeOrderService chargeOrderService;
    private final DrugService drugService;
    private final MemberService memberService;

    public StatisticsController(PatientService patientService, RegistrationService registrationService, ChargeOrderService chargeOrderService, DrugService drugService, MemberService memberService) {
        this.patientService = patientService;
        this.registrationService = registrationService;
        this.chargeOrderService = chargeOrderService;
        this.drugService = drugService;
        this.memberService = memberService;
    }

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        BigDecimal income = chargeOrderService.list(null).stream()
                .map(ChargeOrder::getPaidAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        DashboardVO dashboardVO = new DashboardVO(
                patientService.count(null),
                registrationService.count(null),
                drugService.count(null),
                memberService.count(null),
                income
        );
        return Result.success(dashboardVO);
    }
}
`);

write(path.join(serverJava, "com", "guet", "clinic", "server", "exception", "GlobalExceptionHandler.java"), `package com.guet.clinic.server.exception;

import com.guet.clinic.common.constant.MessageConstant;
import com.guet.clinic.common.exception.BaseException;
import com.guet.clinic.common.result.Result;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BaseException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> business(BaseException exception) {
        return Result.fail(400, exception.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> validation(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(error -> error.getField() + " " + error.getDefaultMessage())
                .orElse(MessageConstant.VALIDATION_FAILED);
        return Result.fail(400, message);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> unknown(Exception exception) {
        return Result.fail(500, exception.getMessage() == null ? MessageConstant.UNKNOWN_ERROR : exception.getMessage());
    }
}
`);
