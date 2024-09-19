package com.hamza.fruitsappbackend.modulus.role.controller;

import com.hamza.fruitsappbackend.modulus.role.dto.RoleDto;
import com.hamza.fruitsappbackend.modulus.role.dto.RolesResponseDto;
import com.hamza.fruitsappbackend.modulus.role.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PostMapping("/create")
    public ResponseEntity<RoleDto> createRole(@RequestHeader("Authorization") String token, @Valid @RequestBody RoleDto roleDto) {
        RoleDto createdRole = roleService.createRole(roleDto, token);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<RoleDto> updateRole(@RequestHeader("Authorization") String token, @PathVariable Long id, @Valid @RequestBody RoleDto roleDto) {
        RoleDto updatedRole = roleService.updateRole(id, roleDto, token);
        return new ResponseEntity<>(updatedRole, HttpStatus.OK);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteRole(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        roleService.deleteRole(id, token);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long id) {
        RoleDto roleDto = roleService.getRoleById(id);
        return new ResponseEntity<>(roleDto, HttpStatus.OK);
    }

    @GetMapping("/all")
    public ResponseEntity<RolesResponseDto> getAllRoles() {
        List<RoleDto> roles = roleService.getAllRoles();
        RolesResponseDto response = new RolesResponseDto(roles, roles.size());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RoleDto>> getRolesByUserId(@PathVariable Long userId) {
        List<RoleDto> roles = roleService.getRolesByUserId(userId);
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

}