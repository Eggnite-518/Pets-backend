package com.example.pets_backend.frameworks.web;

import com.example.pets_backend.frameworks.convention.errorcode.BaseErrorCode;
import com.example.pets_backend.frameworks.convention.exception.AbstractException;
import com.example.pets_backend.frameworks.convention.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理框架定义的业务异常
     *
     * @param exception 业务异常
     * @return 统一响应
     */
    @ExceptionHandler(AbstractException.class)
    public Result<Void> handleAbstractException(AbstractException exception) {
        log.warn("business exception: {}", exception.getMessage());
        return Results.failure(exception);
    }

    /**
     * 处理上传文件超出大小限制
     *
     * @param exception 上传异常
     * @return 统一响应
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exception) {
        log.warn("upload file too large: {}", exception.getMessage());
        return Results.failure("A001402", "图片文件过大，请压缩后重试（单张不超过20MB）");
    }

    /**
     * 兜底处理未知异常
     *
     * @param throwable 未知异常
     * @return 统一响应
     */
    @ExceptionHandler(Throwable.class)
    public Result<Void> handleThrowable(Throwable throwable) {
        log.error("unexpected exception", throwable);
        return Results.failure(BaseErrorCode.SERVICE_ERROR.code(), BaseErrorCode.SERVICE_ERROR.message());
    }
}
