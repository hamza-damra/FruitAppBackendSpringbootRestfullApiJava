package com.hamza.fruitsappbackend.modules.user.service;

import com.hamza.fruitsappbackend.modules.user.dto.ChangePassword;

public interface ForgotPasswordService {

    void sendVerificationEmail(String email);

    String resetPassword(ChangePassword changePassword, Integer otp);
}
