package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.MailBody;
import com.hamza.fruitsappbackend.service.EmailService;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

   private final JavaMailSender mailSender;


    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(MailBody mailBody) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(mailBody.to());
        message.setFrom("hamza.damra@students.alquds.edu");
        message.setSubject(mailBody.subject());
        message.setText(mailBody.body());

        mailSender.send(message);
    }
}
