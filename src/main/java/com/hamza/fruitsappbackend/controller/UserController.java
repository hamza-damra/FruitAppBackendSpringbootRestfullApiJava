package com.hamza.fruitsappbackend.controller;

import com.hamza.fruitsappbackend.dto.JwtAuthResponseDtoLogin;
import com.hamza.fruitsappbackend.dto.UserDTO;
import com.hamza.fruitsappbackend.exception.BadRequestException;
import com.hamza.fruitsappbackend.service.UserService;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(@Valid @RequestBody UserDTO userDTO) {
        logger.info("Registering user");
        UserDTO createdUser = userService.saveUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtAuthResponseDtoLogin> loginUser(@RequestParam String email, @RequestParam String password) {
        logger.info("User login attempt: " + email);
        try {
            // Check if the user's email is verified
            UserDTO user = userService.getUserByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found with email: " + email));

            if (!user.getIsVerified()) {
               throw new BadRequestException("User is not verified. Please verify your email.");
            }

            // Authenticate the user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password));

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(authentication);
            return new ResponseEntity<>(new JwtAuthResponseDtoLogin(token), HttpStatus.OK);
        } catch (AuthenticationException e) {
            logger.severe("User not authenticated: " + e.getMessage());
            return new ResponseEntity<>(new JwtAuthResponseDtoLogin("User not authenticated"), HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping("/sendVerificationEmail/{email}")
    public ResponseEntity<String> sendVerificationEmail(@PathVariable String email) {
        userService.sendVerificationEmail(email);
        return ResponseEntity.ok("Verification email sent successfully.");
    }

    @PostMapping("/verifyAccount/{otp}")
    public ResponseEntity<String> verifyAccount(@PathVariable Integer otp) {
        String result = userService.verifyAccount(otp);

        if ("Account verified successfully.".equals(result)) {
            return ResponseEntity.ok("Account verified successfully. Please log in.");
        } else {
            return new ResponseEntity<>(result, HttpStatus.EXPECTATION_FAILED);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        Optional<UserDTO> userDTO = userService.getUserById(id, token);
        return userDTO.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader("Authorization") String token) {
        List<UserDTO> users = userService.getAllUsers(token);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<UserDTO> updateUser(@RequestHeader("Authorization") String token, @PathVariable Long id, @RequestBody UserDTO userDTO) {
        userDTO.setId(id);
        UserDTO updatedUser = userService.updateUser(userDTO, token);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteUserById(@RequestHeader("Authorization") String token, @PathVariable Long id) {
        userService.deleteUserById(id, token);
        return ResponseEntity.ok("User deleted successfully!");
    }

    @DeleteMapping("/email/{email}")
    public ResponseEntity<String> deleteUserByEmail(@RequestHeader("Authorization") String token, @PathVariable String email) {
        userService.deleteUserByEmail(email, token);
        return ResponseEntity.ok("User deleted successfully!");
    }
}
