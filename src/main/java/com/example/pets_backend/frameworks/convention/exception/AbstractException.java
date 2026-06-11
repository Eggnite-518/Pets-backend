package com.example.pets_backend.frameworks.convention.exception;

import com.example.pets_backend.frameworks.convention.errorcode.IErrorCode;

/**
 * 统一异常抽象基类
 */
public abstract class AbstractException extends RuntimeException {

    private final String errorCode;
    private final String errorMessage;

    protected AbstractException(String message, Throwable cause, IErrorCode errorCode) {
        super(message != null && !message.isBlank() ? message : errorCode.message(), cause);
        this.errorCode = errorCode.code();
        this.errorMessage = message != null && !message.isBlank() ? message : errorCode.message();
    }

    /**
     * 返回错误码
     *
     * @return 错误码
     */
    public final String getErrorCode() {
        return errorCode;
    }

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    public final String getErrorMessage() {
        return errorMessage;
    }
}
