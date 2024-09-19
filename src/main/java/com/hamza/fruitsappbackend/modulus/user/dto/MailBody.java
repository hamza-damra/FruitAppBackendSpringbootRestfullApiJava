package com.hamza.fruitsappbackend.modulus.user.dto;

import lombok.Builder;

@Builder
public record MailBody(String to, String subject, String body) {
}
