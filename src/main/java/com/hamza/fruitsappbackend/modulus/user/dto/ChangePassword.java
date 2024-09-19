package com.hamza.fruitsappbackend.modulus.user.dto;

import lombok.Builder;

@Builder
public record ChangePassword(String newPassword, String confirmPassword) {}
