package com.hamza.fruitsappbackend.modulus.user.service;

import com.hamza.fruitsappbackend.modulus.user.dto.ChangePassword;

public interface ForgotPasswordService {

    void sendVerificationEmail(String email);

    String resetPassword(ChangePassword changePassword, Integer otp);
}
