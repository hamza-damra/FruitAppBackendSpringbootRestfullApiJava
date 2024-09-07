package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Optional;

public interface UserService extends UserDetailsService {

    // Save a new user
    UserDTO saveUser(UserDTO userDTO);

    // Retrieve a user by their ID, with authorization token check
    Optional<UserDTO> getUserById(Long id, String token);

    // Retrieve a user by their email, with authorization token check
    Optional<UserDTO> getUserByEmail(String email, String token);

    // Retrieve a user by their email without token (for login purposes)
    Optional<UserDTO> getUserByEmail(String email);

    // Retrieve all users with authorization token check
    List<UserDTO> getAllUsers(String token);

    // Update a user with authorization token check
    UserDTO updateUser(UserDTO userDTO, String token);

    // Delete a user by their ID with authorization token check
    void deleteUserById(Long id, String token);

    // Delete a user by their email with authorization token check
    void deleteUserByEmail(String email, String token);

    // Send verification email to the user
    void sendVerificationEmail(String email);

    // Verify the user account using OTP
    String verifyAccount(Integer otp);

    // Retrieve user by OTP (for verification purposes)
    UserDTO getUserByOtp(Integer otp);
}
