package com.hamza.fruitsappbackend.dto;

import lombok.Builder;

@Builder
public record ChangePassword(String newPassword, String confirmPassword) {}
