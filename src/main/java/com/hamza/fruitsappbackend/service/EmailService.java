package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.MailBody;


public interface EmailService {

    void sendEmail(MailBody mailBody);

}
