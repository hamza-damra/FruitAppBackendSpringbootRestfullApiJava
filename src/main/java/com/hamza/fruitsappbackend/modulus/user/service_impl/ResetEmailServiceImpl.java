package com.hamza.fruitsappbackend.modulus.user.service_impl;

import com.hamza.fruitsappbackend.modulus.user.dto.ChangeEmail;
import com.hamza.fruitsappbackend.modulus.user.dto.MailBody;
import com.hamza.fruitsappbackend.modulus.user.entity.ResetEmail;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import com.hamza.fruitsappbackend.modulus.user.repository.ResetEmailRepository;
import com.hamza.fruitsappbackend.modulus.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modulus.user.service.EmailService;
import com.hamza.fruitsappbackend.modulus.user.service.ResetEmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@Service
public class ResetEmailServiceImpl implements ResetEmailService {
    private static final Logger logger = LoggerFactory.getLogger(ResetEmailServiceImpl.class);
    private final UserRepository userRepository;
    private final EmailService mailService;
    private final ResetEmailRepository resetEmailRepository;

    @Autowired
    public ResetEmailServiceImpl(UserRepository userRepository, EmailService mailService,
                                 ResetEmailRepository resetEmailRepository) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.resetEmailRepository = resetEmailRepository;

    }

    @Override
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        Integer otp = generateOtp();

        ResetEmail resetEmail = resetEmailRepository.findByUser(user)
                .map(existingForgotPassword -> {
                    existingForgotPassword.setOtp(otp);
                    existingForgotPassword.setExpirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000)));
                    return existingForgotPassword;
                })
                .orElse(ResetEmail.builder()
                        .user(user)
                        .otp(otp)
                        .expirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000)))  // 1-hour expiration
                        .build());

        resetEmailRepository.save(resetEmail);

        String htmlBody = createEmailBody(otp);

        MailBody mailBody = MailBody.builder()
                .to(user.getEmail())
                .subject("Reset Email")
                .body(htmlBody)
                .build();

        mailService.sendEmail(mailBody);
        logger.info("Email reset email sent to {}", user.getEmail());
    }

    @Override
    public String resetEmail(ChangeEmail changeEmail, Integer otp) {
        if (!Objects.equals(changeEmail.newEmail(), changeEmail.confirmEmail())) {
            return "Email do not match.";
        }

        Optional<ResetEmail> resetEmailOptional = resetEmailRepository
                .findByOtpAndExpirationTimeAfter(otp, new Date());

        if (resetEmailOptional.isEmpty()) {
            return "Invalid OTP or expired link.";
        }

        ResetEmail resetEmail = resetEmailOptional.get();
        User user = resetEmail.getUser();

        user.setEmail(changeEmail.newEmail());
        userRepository.save(user);

        resetEmailRepository.delete(resetEmail);

        logger.info("Email reset successfully for user: {}", user.getEmail());

        return "Email reset successfully.";
    }

    private Integer generateOtp() {
        return (int) (Math.random() * 9000) + 1000;
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
                "<p>We received a request to reset your email for your Fruits Application account. To proceed, please use the verification code below:</p>" +
                "<div class=\"otp-code\">" + otp + "</div>" +
                "<p>Enter this code on the email reset page to reset your email.</p>" +
                "<p>If you did not request a email reset, please ignore this email or contact our support team if you have any concerns.</p>" +
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
