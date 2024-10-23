package com.hamza.fruitsappbackend.modules.user.dto;

import lombok.Builder;

@Builder
public record ChangePassword(String newPassword, String confirmPassword) {}
