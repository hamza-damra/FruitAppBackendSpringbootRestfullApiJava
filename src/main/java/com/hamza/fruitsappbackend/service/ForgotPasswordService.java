package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.ChangePassword;

public interface ForgotPasswordService {

    void sendVerificationEmail(String email);

    String resetPassword(ChangePassword changePassword, Integer otp);
}
