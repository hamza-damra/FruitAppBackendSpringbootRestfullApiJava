package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    UserDTO saveUser(UserDTO userDTO);

    Optional<UserDTO> getUserById(Long id, String token);

    Optional<UserDTO> getUserByEmail(String email, String token);

    List<UserDTO> getAllUsers(String token);

    UserDTO updateUser(UserDTO userDTO, String token);

    void deleteUserById(Long id, String token);

    void deleteUserByEmail(String email, String token);
}
