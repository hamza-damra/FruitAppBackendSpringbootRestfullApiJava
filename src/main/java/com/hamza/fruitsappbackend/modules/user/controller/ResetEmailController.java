package com.hamza.fruitsappbackend.modules.user.controller;

import com.hamza.fruitsappbackend.modules.user.dto.ChangeEmail;
import com.hamza.fruitsappbackend.modules.user.service.ResetEmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resetEmail")
public class ResetEmailController {
    private final ResetEmailService resetEmailService;

    @Autowired
    public ResetEmailController(ResetEmailService resetEmailService) {
        this.resetEmailService = resetEmailService;
    }


    @PostMapping("/verifyMail/{email}")
    public ResponseEntity<String> verifyMail(@PathVariable String email) {
        resetEmailService.sendVerificationEmail(email);
        return ResponseEntity.ok("An email has been sent to your registered email address.");
    }

    @PostMapping("/resetEmail/{otp}")
    public ResponseEntity<String> resetEmail(@RequestBody ChangeEmail changeEmail, @PathVariable Integer otp) {
        String result = resetEmailService.resetEmail(changeEmail, otp);

        if ("Email reset successfully.".equals(result)) {
            return ResponseEntity.ok(result);
        } else {
            return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

}
