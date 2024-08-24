package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.MailBody;
import com.hamza.fruitsappbackend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendEmail(MailBody mailBody) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(mailBody.to());
            helper.setFrom("hamza.damra@students.alquds.edu");
            helper.setSubject(mailBody.subject());
            helper.setText(mailBody.body(), true);

            mailSender.send(message);
            System.out.println("HTML email sent successfully!");

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Failed to send email due to an error.");
        }
    }
}
