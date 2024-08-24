package com.hamza.fruitsappbackend.service;

public interface AccountVerificationService {

    void sendVerificationEmail(String email);

    String verifyAccount(Integer otp);
}
