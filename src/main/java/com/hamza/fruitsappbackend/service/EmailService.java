package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.MailBody;
import org.springframework.stereotype.Service;


public interface EmailService {

    void sendEmail(MailBody mailBody);

}
