package com.hamza.fruitsappbackend.modules.user.service;

import com.hamza.fruitsappbackend.modules.user.dto.ChangeEmail;

public interface ResetEmailService {

    void sendVerificationEmail(String email);

    String resetEmail(ChangeEmail changePassword, Integer otp);

}
