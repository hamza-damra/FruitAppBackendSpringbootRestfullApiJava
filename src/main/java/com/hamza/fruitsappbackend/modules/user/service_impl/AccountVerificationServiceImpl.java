package com.hamza.fruitsappbackend.modules.user.service_impl;

import com.hamza.fruitsappbackend.modules.user.dto.MailBody;
import com.hamza.fruitsappbackend.modules.user.entity.AccountVerification;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modules.user.repository.AccountVerificationRepository;
import com.hamza.fruitsappbackend.modules.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modules.user.service.AccountVerificationService;
import com.hamza.fruitsappbackend.modules.user.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
public class AccountVerificationServiceImpl implements AccountVerificationService {

    private final UserRepository userRepository;
    private final EmailService mailService;
    private final AccountVerificationRepository accountVerificationRepository;

    @Autowired
    public AccountVerificationServiceImpl(UserRepository userRepository, EmailService mailService, AccountVerificationRepository accountVerificationRepository) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.accountVerificationRepository = accountVerificationRepository;
    }

    @Override
    public void sendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        Integer otp = generateOtp();

        // التحقق مما إذا كان المستخدم لديه تحقق موجود مسبقًا
        AccountVerification accountVerification = accountVerificationRepository.findByUser(user)
                .map(existingVerification -> {
                    existingVerification.setOtp(otp);
                    existingVerification.setExpirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000)));
                    return existingVerification;
                })
                .orElse(AccountVerification.builder()
                        .user(user)
                        .otp(otp)
                        .expirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000)))
                        .build());

        accountVerificationRepository.save(accountVerification);

        // إنشاء محتوى البريد الإلكتروني
        String htmlBody = createEmailBody(otp);
        MailBody mailBody = MailBody.builder()
                .to(user.getEmail())
                .subject("Account Verification")
                .body(htmlBody)
                .build();

        mailService.sendEmail(mailBody);
    }

    @Override
    public String verifyAccount(Integer otp) {
        Optional<AccountVerification> verificationOptional = accountVerificationRepository
                .findByOtpAndExpirationTimeAfter(otp, new Date());

        if (verificationOptional.isEmpty()) {
            throw new BadRequestException("Invalid OTP or expired OTP.");
        }

        AccountVerification accountVerification = verificationOptional.get();
        User user = accountVerification.getUser();
        user.setIsVerified(true);

        userRepository.save(user);
        accountVerificationRepository.delete(accountVerification);

        return "Account verified successfully.";
    }

    private Integer generateOtp() {
        return (int) (Math.random() * 9000) + 1000;  // توليد OTP بين 1000 و 9999
    }

    // إنشاء قالب البريد الإلكتروني
    private String createEmailBody(Integer otp) {
        String pngImageUrl = "https://firebasestorage.googleapis.com/v0/b/testing-18f33.appspot.com/o/image.png?alt=media&token=ed7d8c51-10e5-42c9-8c57-79eb031e5858";
        return "<!DOCTYPE html>" +
                "<html lang=\"en\">" +
                "<head>" +
                "<meta charset=\"UTF-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<title>Account Verification</title>" +
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
                "<p>Welcome to Fruits Application! We're excited to have you join our community. To complete your registration, please use the verification code below:</p>" +
                "<div class=\"otp-code\">" + otp + "</div>" +
                "<p>Enter this code on the verification page to verify your account.</p>" +
                "<p>If you did not sign up for a Fruits Application account, please ignore this email or contact our support team if you have any questions.</p>" +
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
