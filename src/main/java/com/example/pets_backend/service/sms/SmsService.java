package com.example.pets_backend.service.sms;

public interface SmsService {

    void sendCode(String phone, String code);
}
