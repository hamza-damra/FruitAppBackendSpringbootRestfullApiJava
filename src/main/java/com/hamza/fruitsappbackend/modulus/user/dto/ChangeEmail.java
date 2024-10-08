package com.hamza.fruitsappbackend.modulus.user.dto;

import lombok.Builder;

@Builder
public record ChangeEmail(String newEmail, String confirmEmail) {}