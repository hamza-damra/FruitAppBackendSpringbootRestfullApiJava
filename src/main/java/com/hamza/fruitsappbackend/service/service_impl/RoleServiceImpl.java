package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.RoleDto;
import com.hamza.fruitsappbackend.entity.Role;
import com.hamza.fruitsappbackend.repository.RoleRepository;
import com.hamza.fruitsappbackend.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public RoleDto createRole(RoleDto roleDto) {
        Role role = modelMapper.map(roleDto, Role.class);
        role.setName("ROLE_" + role.getName().toUpperCase());
        role = roleRepository.save(role);
        return modelMapper.map(role, RoleDto.class);
    }

    @Override
    @Transactional
    public RoleDto updateRole(Long id, RoleDto roleDto) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        // Update the role properties
        existingRole.setName(roleDto.getName().toUpperCase());

        Role updatedRole = roleRepository.save(existingRole);
        return modelMapper.map(updatedRole, RoleDto.class);
    }

    @Override
    @Transactional
    public void deleteRole(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));

        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Role not found with id: " + id));
        return modelMapper.map(role, RoleDto.class);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream().map(role -> modelMapper.map(role, RoleDto.class)).toList();
    }
}
