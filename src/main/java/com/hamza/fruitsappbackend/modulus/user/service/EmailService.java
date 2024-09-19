package com.hamza.fruitsappbackend.modulus.user.service;

import com.hamza.fruitsappbackend.modulus.user.dto.MailBody;


public interface EmailService {

    void sendEmail(MailBody mailBody);

}
