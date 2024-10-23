package com.hamza.fruitsappbackend.modules.user.dto;

import lombok.Builder;

@Builder
public record ChangeEmail(String newEmail, String confirmEmail) {}