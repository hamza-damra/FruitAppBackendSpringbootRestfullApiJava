package com.hamza.fruitsappbackend.modules.user.service;

import com.hamza.fruitsappbackend.modules.user.dto.MailBody;


public interface EmailService {

    void sendEmail(MailBody mailBody);

}
