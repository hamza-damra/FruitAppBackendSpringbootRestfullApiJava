package com.hamza.fruitsappbackend.modules.user.dto;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String body) {}
