package com.example.pets_backend.frameworks.convention.errorcode;

/**
 * 错误码接口
 */
public interface IErrorCode {

    /**
     * 返回错误码
     *
     * @return 错误码
     */
    String code();

    /**
     * 返回错误消息
     *
     * @return 错误消息
     */
    String message();
}
