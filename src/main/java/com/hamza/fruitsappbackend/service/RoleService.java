package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.RoleDto;

import java.util.List;

public interface RoleService {

    RoleDto createRole(RoleDto roleDto, String token);

    RoleDto updateRole(Long id, RoleDto roleDto, String token);

    void deleteRole(Long id, String token);

    RoleDto getRoleById(Long id);

    List<RoleDto> getAllRoles();

    List<RoleDto> getRolesByUserId(Long userId);
}
