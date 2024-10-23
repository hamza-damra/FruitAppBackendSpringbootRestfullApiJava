package com.hamza.fruitsappbackend.modules.user.service;

public interface AccountVerificationService {

    void sendVerificationEmail(String email);

    String verifyAccount(Integer otp);
}
