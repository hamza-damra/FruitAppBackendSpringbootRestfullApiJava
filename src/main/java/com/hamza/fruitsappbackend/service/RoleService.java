package com.hamza.fruitsappbackend.service;


import com.hamza.fruitsappbackend.dto.RoleDto;

import java.util.List;

public interface RoleService {
    RoleDto createRole(RoleDto roleDto);

    RoleDto updateRole(Long id, RoleDto roleDto);

    void deleteRole(Long id);

    RoleDto getRoleById(Long id);

    List<RoleDto> getAllRoles();

    List<RoleDto> getRolesByUserId(Long userId);
}