package com.guet.clinic.common.result;

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
