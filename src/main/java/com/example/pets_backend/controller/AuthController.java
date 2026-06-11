package com.example.pets_backend.controller;

import com.example.pets_backend.dto.req.ChangePasswordReqDTO;
import com.example.pets_backend.dto.req.LoginByCodeReqDTO;
import com.example.pets_backend.dto.req.LoginUserReqDTO;
import com.example.pets_backend.dto.req.RegisterUserReqDTO;
import com.example.pets_backend.dto.req.SendCodeReqDTO;
import com.example.pets_backend.dto.req.SetPasswordReqDTO;
import com.example.pets_backend.dto.resp.LoginUserRespDTO;
import com.example.pets_backend.dto.resp.RegisterUserRespDTO;
import com.example.pets_backend.frameworks.convention.result.Result;
import com.example.pets_backend.frameworks.web.Results;
import com.example.pets_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    public Result<LoginUserRespDTO> login(@RequestBody LoginUserReqDTO reqDTO) {
        return Results.success(userService.login(reqDTO));
    }

    @PostMapping("/register")
    public Result<RegisterUserRespDTO> register(@RequestBody RegisterUserReqDTO reqDTO) {
        return Results.success(userService.register(reqDTO));
    }

    @PostMapping("/send-code")
    public Result<Void> sendCode(@RequestBody SendCodeReqDTO reqDTO) {
        userService.sendVerificationCode(reqDTO.phone());
        return Results.success();
    }

    @PostMapping("/login-by-code")
    public Result<LoginUserRespDTO> loginByCode(@RequestBody LoginByCodeReqDTO reqDTO) {
        return Results.success(userService.loginByCode(reqDTO));
    }

    @PostMapping("/set-password")
    public Result<Void> setPassword(@RequestBody SetPasswordReqDTO reqDTO) {
        userService.setPassword(reqDTO);
        return Results.success();
    }

    @PostMapping("/change-password")
    public Result<Void> changePassword(@RequestBody ChangePasswordReqDTO reqDTO) {
        userService.changePassword(reqDTO);
        return Results.success();
    }
}
