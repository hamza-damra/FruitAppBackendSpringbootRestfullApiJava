package com.hamza.fruitsappbackend.modules.user.service_impl;

import com.hamza.fruitsappbackend.modules.user.dto.MailBody;
import com.hamza.fruitsappbackend.modules.user.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    @Async
    public void sendEmail(MailBody mailBody) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            // إعداد رسالة البريد الإلكتروني
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(mailBody.to());
            helper.setFrom("hamza.damra@students.alquds.edu");
            helper.setSubject(mailBody.subject());
            helper.setText(mailBody.body(), true);

            // إرسال الرسالة
            mailSender.send(message);
            logger.info("HTML email sent successfully to {}", mailBody.to());

        } catch (MessagingException e) {
            logger.error("Failed to send email to {} due to an error: {}", mailBody.to(), e.getMessage());
        }
    }
}
