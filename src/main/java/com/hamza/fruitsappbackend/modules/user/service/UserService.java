package com.hamza.fruitsappbackend.modules.user.service;

import com.hamza.fruitsappbackend.modules.user.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    UserDTO addUser(UserDTO userDTO);

    Optional<UserDTO> getUserById(Long id, String token);

    Optional<UserDTO> getUserByEmail(String email, String token);

    Optional<UserDTO> getUserByEmail(String email);

    List<UserDTO> getAllUsers(String token);

    UserDTO updateUser(UserDTO userDTO, String token);

    void deleteUserById(Long id, String token);

    void deleteUserByEmail(String email, String token);

    void sendVerificationEmail(String email);

    String verifyAccount(Integer otp);

    UserDTO getUserByOtp(Integer otp);
}
