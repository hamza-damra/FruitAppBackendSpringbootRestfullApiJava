package com.hamza.fruitsappbackend.utils;

import com.hamza.fruitsappbackend.modules.user.entity.User;
import com.hamza.fruitsappbackend.modules.user.repository.UserRepository;
import com.hamza.fruitsappbackend.security.JwtTokenProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AuthorizationUtils {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthorizationUtils.class);

    public AuthorizationUtils(JwtTokenProvider jwtTokenProvider, UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
    }

    public User getUserFromToken(String token) {
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        logger.info("Received token: '{}'", token);
        String username = jwtTokenProvider.getUserNameFromToken(token);
        return userRepository.findByEmail(username)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
    }

    public void checkUserOrAdminRole(String token, Long userId) {
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User user = getUserFromToken(token);

        if (!user.getId().equals(userId) && user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }


    public void checkAdminRole(String token) {
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User user = getUserFromToken(token);

        if (user.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }

    public void checkUserOrAdminRoleByEmail(String token, String email) {
        token = token.trim();
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        User requestingUser = getUserFromToken(token);

        if (!requestingUser.getEmail().equals(email) && requestingUser.getRoles().stream()
                .noneMatch(role -> role.getName().equals("ROLE_ADMIN"))) {
            throw new AccessDeniedException("You do not have the necessary permissions to perform this operation");
        }
    }
}
