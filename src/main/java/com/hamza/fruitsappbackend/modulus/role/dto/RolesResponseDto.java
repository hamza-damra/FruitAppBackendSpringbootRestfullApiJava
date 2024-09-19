package com.hamza.fruitsappbackend.modulus.role.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RolesResponseDto {
    private List<RoleDto> roles;
    private int rolesCount;
}
