package com.example.pets_backend.frameworks.auth;

import java.util.Optional;

/**
 * 当前请求用户上下文
 */
public final class UserContext {

    private static final ThreadLocal<UserInfoDTO> USER_THREAD_LOCAL = new ThreadLocal<>();

    private UserContext() {
    }

    public static void setUser(UserInfoDTO userInfo) {
        USER_THREAD_LOCAL.set(userInfo);
    }

    public static UserInfoDTO getUser() {
        return USER_THREAD_LOCAL.get();
    }

    public static Long getUserId() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get())
                .map(UserInfoDTO::userId)
                .orElse(null);
    }

    public static String getPhone() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get())
                .map(UserInfoDTO::phone)
                .orElse(null);
    }

    public static String getNickname() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get())
                .map(UserInfoDTO::nickname)
                .orElse(null);
    }

    public static Integer getRoleType() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get())
                .map(UserInfoDTO::roleType)
                .orElse(null);
    }

    public static String getToken() {
        return Optional.ofNullable(USER_THREAD_LOCAL.get())
                .map(UserInfoDTO::token)
                .orElse(null);
    }

    public static void clear() {
        USER_THREAD_LOCAL.remove();
    }
}

