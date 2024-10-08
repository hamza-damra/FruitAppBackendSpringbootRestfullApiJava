package com.hamza.fruitsappbackend.modulus.user.service;

import com.hamza.fruitsappbackend.modulus.user.dto.ChangeEmail;
import com.hamza.fruitsappbackend.modulus.user.dto.ChangePassword;

public interface ResetEmailService {

    void sendVerificationEmail(String email);

    String resetEmail(ChangeEmail changePassword, Integer otp);

}
