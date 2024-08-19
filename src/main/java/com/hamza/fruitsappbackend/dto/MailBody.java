package com.hamza.fruitsappbackend.dto;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String body) {
}
