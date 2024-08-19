package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.ChangePassword;
import com.hamza.fruitsappbackend.dto.MailBody;
import com.hamza.fruitsappbackend.entity.ForgotPassword;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.repository.ForgotPasswordRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/forgotPassword")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final EmailService mailService;
    private final ForgotPasswordRepository forgotPasswordRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public ForgotPasswordController(UserRepository userRepository, EmailService mailService, ForgotPasswordRepository forgotPasswordRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailService = mailService;
        this.forgotPasswordRepository = forgotPasswordRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyMail(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Please provide a valid email"));

        Integer otp = generateOtp();

        // Check if there's already an OTP entry for this user
        Optional<ForgotPassword> existingForgotPassword = forgotPasswordRepository.findByUser(user);

        ForgotPassword forgotPassword;
        if (existingForgotPassword.isPresent()) {
            // Update existing OTP entry
            forgotPassword = existingForgotPassword.get();
            forgotPassword.setOtp(otp);
            forgotPassword.setExpirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000))); // 1-hour expiration
        } else {
            // Create a new OTP entry
            forgotPassword = ForgotPassword.builder()
                    .user(user)
                    .otp(otp)
                    .expirationTime(new Date(System.currentTimeMillis() + (60 * 60 * 1000))) // 1-hour expiration
                    .build();
        }

        forgotPasswordRepository.save(forgotPassword);

        // Prepare and send the email
        MailBody mailBody = MailBody.builder()
                .to(user.getEmail())
                .subject("Reset Password")
                .body("Click here to reset your password: http://localhost:8080/api/v1/forgotPassword/resetPassword/" + otp)
                .build();

        mailService.sendEmail(mailBody);

        return ResponseEntity.ok("An email has been sent to your registered email address.");
    }


    @PostMapping("/resetPassword/{otp}")
    public ResponseEntity<String> resetPassword(@RequestBody ChangePassword changePassword, @PathVariable Integer otp) {

        // Check if the new password and confirm password match
        if (!Objects.equals(changePassword.newPassword(), changePassword.confirmPassword())) {
            return new ResponseEntity<>("Passwords do not match.", HttpStatus.EXPECTATION_FAILED);
        }

        // Find the ForgotPassword entry by OTP and expiration time
        Optional<ForgotPassword> forgotPasswordOptional = forgotPasswordRepository
                .findByOtpAndExpirationTimeAfter(otp, new Date());

        // Check if the OTP is valid and not expired
        if (forgotPasswordOptional.isEmpty()) {
            return new ResponseEntity<>("Invalid OTP or expired link.", HttpStatus.EXPECTATION_FAILED);
        }

        ForgotPassword forgotPassword = forgotPasswordOptional.get();
        User user = forgotPassword.getUser();

        // Encode the new password and update the user entity
        String encodedPassword = passwordEncoder.encode(changePassword.newPassword());
        user.setPassword(encodedPassword);

        // Save the updated user entity
        userRepository.save(user);

        // Delete the OTP entry from the database after a successful password reset
        forgotPasswordRepository.delete(forgotPassword);

        // Return a successful response
        return ResponseEntity.ok("Password reset successfully.");
    }





    private Integer generateOtp() {
        return (int) (Math.random() * 900000) + 100000;
    }






}
