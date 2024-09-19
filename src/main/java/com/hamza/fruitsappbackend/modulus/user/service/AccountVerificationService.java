package com.hamza.fruitsappbackend.modulus.user.service;

public interface AccountVerificationService {

    void sendVerificationEmail(String email);

    String verifyAccount(Integer otp);
}
