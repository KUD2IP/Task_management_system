package org.example.authenticationservice.service.contract;

public interface VerificationCodeService {

    void sendCode(String email);
    void verify(String email, String code);
}