package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.RoleDto;
import com.hamza.fruitsappbackend.entity.Role;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.exception.RoleNotFoundException;
import com.hamza.fruitsappbackend.exception.RoleDeletionException;
import com.hamza.fruitsappbackend.repository.RoleRepository;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.RoleService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Override
    @Transactional
    public RoleDto createRole(RoleDto roleDto, String token) {
        authorizationUtils.checkAdminRole(token);

        Role role = modelMapper.map(roleDto, Role.class);
        role.setName("ROLE_" + role.getName().toUpperCase());
        role = roleRepository.save(role);
        return modelMapper.map(role, RoleDto.class);
    }

    @Override
    @Transactional
    public RoleDto updateRole(Long id, RoleDto roleDto, String token) {
        authorizationUtils.checkAdminRole(token);

        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("id", id.toString()));

        existingRole.setName("ROLE_" + roleDto.getName().toUpperCase());

        Role updatedRole = roleRepository.save(existingRole);
        return modelMapper.map(updatedRole, RoleDto.class);
    }

    @Override
    @Transactional
    public void deleteRole(Long id, String token) {
        authorizationUtils.checkAdminRole(token);

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("id", id.toString()));

        if (!role.getUsers().isEmpty()) {
            throw new RoleDeletionException("Role cannot be deleted as it is associated with users.");
        }

        roleRepository.delete(role);
    }

    @Override
    @Transactional
    public RoleDto getRoleById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException("id", id.toString()));
        return modelMapper.map(role, RoleDto.class);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(role -> modelMapper.map(role, RoleDto.class))
                .toList();
    }

    @Override
    @Transactional
    public List<RoleDto> getRolesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RoleNotFoundException("id", userId.toString()));

        Set<Role> roles = user.getRoles();
        return roles.stream()
                .map(role -> modelMapper.map(role, RoleDto.class))
                .toList();
    }
}
