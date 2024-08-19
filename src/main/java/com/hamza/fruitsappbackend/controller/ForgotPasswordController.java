package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.ChangePassword;
import com.hamza.fruitsappbackend.service.ForgotPasswordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/forgotPassword")
public class ForgotPasswordController {

    private final ForgotPasswordService forgotPasswordService;

    @Autowired
    public ForgotPasswordController(ForgotPasswordService forgotPasswordService) {
        this.forgotPasswordService = forgotPasswordService;
    }

    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyMail(@PathVariable String email) {
        forgotPasswordService.sendVerificationEmail(email);
        return ResponseEntity.ok("An email has been sent to your registered email address.");
    }

    @PostMapping("/resetPassword/{otp}")
    public ResponseEntity<String> resetPassword(@RequestBody ChangePassword changePassword, @PathVariable Integer otp) {
        String result = forgotPasswordService.resetPassword(changePassword, otp);

        if ("Password reset successfully.".equals(result)) {
            return ResponseEntity.ok(result);
        } else {
            return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }
}
