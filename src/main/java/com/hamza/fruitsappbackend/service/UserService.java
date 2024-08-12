package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    UserDTO saveUser(UserDTO userDTO);

    Optional<UserDTO> getUserById(Long id);

    Optional<UserDTO> getUserByEmail(String email);

    List<UserDTO> getAllUsers();

    UserDTO updateUser(UserDTO userDTO);

    void deleteUserById(Long id);

    void deleteUserByEmail(String email);
}
