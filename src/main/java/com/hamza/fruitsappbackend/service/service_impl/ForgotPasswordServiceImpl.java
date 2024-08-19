package com.hamza.fruitsappbackend.service.impl;

import com.hamza.fruitsappbackend.dto.ChangePassword;
import com.hamza.fruitsappbackend.dto.MailBody;
import com.hamza.fruitsappbackend.entity.ForgotPassword;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.repository.ForgotPasswordRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.EmailService;
import com.hamza.fruitsappbackend.service.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private final UserRepository userRepository;
    private final EmailService mailService;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ForgotPasswordServiceImpl(UserRepository userRepository, EmailService mailService, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        Integer otp = generateOtp();

        Optional<ForgotPassword> existingForgotPassword = forgotPasswordRepository.findByUser(user);

        ForgotPassword forgotPassword;
        if (existingForgotPassword.isPresent()) {

            forgotPassword = existingForgotPassword.get();
            forgotPassword.setOtp(otp);
            forgotPassword.setExpirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000))); // 1-hour expiration
        } else {

            forgotPassword = ForgotPassword.builder()
                    .user(user)
                    .otp(otp)
                    .expirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000))) // 1-hour expiration
                    .build();
        }

        forgotPasswordRepository.save(forgotPassword);

        MailBody mailBody = MailBody.builder()
                .to(user.getEmail())
                .subject("Reset Password")
                .body("Click here to reset your password: http://localhost:8080/api/v1/forgotPassword/resetPassword/" + otp)
                .build();

        mailService.sendEmail(mailBody);
    }

    @Override
    public String resetPassword(ChangePassword changePassword, Integer otp) {

        if (!Objects.equals(changePassword.newPassword(), changePassword.confirmPassword())) {
            return "Passwords do not match.";
        }

        Optional<ForgotPassword> forgotPasswordOptional = forgotPasswordRepository
                .findByOtpAndExpirationTimeAfter(otp, new Date());

        if (forgotPasswordOptional.isEmpty()) {
            return "Invalid OTP or expired link.";
        }

        ForgotPassword forgotPassword = forgotPasswordOptional.get();
        User user = forgotPassword.getUser();

        String encodedPassword = passwordEncoder.encode(changePassword.newPassword());

        user.setPassword(encodedPassword);

        userRepository.save(user);

        forgotPasswordRepository.delete(forgotPassword);

        return "Password reset successfully.";
    }

    private Integer generateOtp() {
        return (int) (Math.random() * 9000) + 1000;
    }
}
