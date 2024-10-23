package com.hamza.fruitsappbackend.modules.user.service_impl;

import com.hamza.fruitsappbackend.modules.user.dto.ChangePassword;
import com.hamza.fruitsappbackend.modules.user.dto.MailBody;
import com.hamza.fruitsappbackend.modules.user.entity.ForgotPassword;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import com.hamza.fruitsappbackend.modules.user.repository.ForgotPasswordRepository;
import com.hamza.fruitsappbackend.modules.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modules.user.service.EmailService;
import com.hamza.fruitsappbackend.modules.user.service.ForgotPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private static final Logger logger = LoggerFactory.getLogger(ForgotPasswordServiceImpl.class);

    private final UserRepository userRepository;
    private final EmailService mailService;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ForgotPasswordServiceImpl(UserRepository userRepository, EmailService mailService,
                                     ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Async
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        Integer otp = generateOtp();

        ForgotPassword forgotPassword = forgotPasswordRepository.findByUser(user)
                .map(existingForgotPassword -> {
                    existingForgotPassword.setOtp(otp);
                    existingForgotPassword.setExpirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000)));
                    return existingForgotPassword;
                })
                .orElse(ForgotPassword.builder()
                        .user(user)
                        .otp(otp)
                        .expirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000)))  // 1-hour expiration
                        .build());

        forgotPasswordRepository.save(forgotPassword);

        String htmlBody = createEmailBody(otp);

        MailBody mailBody = MailBody.builder()
                .to(user.getEmail())
                .subject("Reset Password")
                .body(htmlBody)
                .build();

        mailService.sendEmail(mailBody);
        logger.info("Password reset email sent to {}", user.getEmail());
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

        user.setPassword(passwordEncoder.encode(changePassword.newPassword()));
        userRepository.save(user);

        forgotPasswordRepository.delete(forgotPassword);

        logger.info("Password reset successfully for user: {}", user.getEmail());

        return "Password reset successfully.";
    }

    private Integer generateOtp() {
        return (int) (Math.random() * 9000) + 1000;  // Generate OTP between 1000 and 9999
    }

    private String createEmailBody(Integer otp) {
        String pngImageUrl = "https://firebasestorage.googleapis.com/v0/b/testing-18f33.appspot.com/o/image.png?alt=media&token=ed7d8c51-10e5-42c9-8c57-79eb031e5858";

        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>" +
                "body {margin: 0; padding: 0; background-color: #e6f7e6; font-family: 'Open Sans', sans-serif; color: #006400;}" +
                ".container {max-width: 550px; margin: 0 auto; background-color: #ffffff; padding: 20px; border-radius: 8px; border-top: 5px solid #006400;}" +
                ".header {text-align: center; margin-bottom: 20px;}" +
                ".header img {width: 132.5px; height: auto;}" +
                ".content {line-height: 1.6; color: #006400;}" +
                ".otp-code {font-size: 24px; font-weight: bold; color: #32cd32; text-align: center; margin: 20px 0;}" +
                ".footer {text-align: center; font-size: 12px; color: #2f4f4f; margin-top: 20px;}" +
                ".footer a {color: #2f4f4f; text-decoration: none;}" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class=\"container\">" +
                "<div class=\"header\">" +
                "<a href=\"https://yourwebsite.com\" target=\"_blank\">" +
                "<img src=\"" + pngImageUrl + "\" alt=\"Fruits Application\" title=\"Fruits Application\">" +
                "</a>" +
                "</div>" +
                "<div class=\"content\">" +
                "<p>Hello,</p>" +
                "<p>We received a request to reset your password for your Fruits Application account. To proceed, please use the verification code below:</p>" +
                "<div class=\"otp-code\">" + otp + "</div>" +
                "<p>Enter this code on the password reset page to reset your password.</p>" +
                "<p>If you did not request a password reset, please ignore this email or contact our support team if you have any concerns.</p>" +
                "</div>" +
                "<div class=\"footer\">" +
                "<p>If this attempt wasn’t you, please email <a href=\"mailto:fruitsappcompany@mail.com\">fruitsappcompany@mail.com</a> for assistance.</p>" +
                "<p>Fruits Application, Inc.</p>" +
                "<p>Palestine, Jericho City P58302 AbuSaker Street</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}
