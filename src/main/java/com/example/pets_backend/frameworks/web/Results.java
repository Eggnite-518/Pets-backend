package com.example.pets_backend.frameworks.web;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.AbstractException;
import com.example.pets_backend.frameworks.convention.result.Result;
import java.util.Optional;

/**
 * 全局返回对象构造器
 */
public final class Results {

    private Results() {
    }

    /**
     * 构造成功响应
     *
     * @return 成功响应
     */
    public static Result<Void> success() {
        return new Result<Void>()
                .setCode(Result.SUCCESS_CODE)
                .setMessage("success");
    }

    /**
     * 构造带返回数据的成功响应
     *
     * @param data 响应数据
     * @param <T> 响应数据类型
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return new Result<T>()
                .setCode(Result.SUCCESS_CODE)
                .setMessage("success")
                .setData(data);
    }

    /**
     * 构建服务端失败响应
     *
     * @return 失败响应
     */
    public static Result<Void> failure() {
        return new Result<Void>()
                .setCode(BaseErrorCode.SERVICE_ERROR.code())
                .setMessage(BaseErrorCode.SERVICE_ERROR.message());
    }

    /**
     * 通过框架异常构建失败响应
     *
     * @param exception 异常对象
     * @return 失败响应
     */
    public static Result<Void> failure(AbstractException exception) {
        String errorCode = Optional.ofNullable(exception.getErrorCode())
                .orElse(BaseErrorCode.SERVICE_ERROR.code());
        String errorMessage = Optional.ofNullable(exception.getErrorMessage())
                .orElse(BaseErrorCode.SERVICE_ERROR.message());
        return new Result<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
    }

    /**
     * 通过错误码和消息构建失败响应
     *
     * @param errorCode 错误码
     * @param errorMessage 错误消息
     * @return 失败响应
     */
    public static Result<Void> failure(String errorCode, String errorMessage) {
        return new Result<Void>()
                .setCode(errorCode)
                .setMessage(errorMessage);
    }
}
