package com.hamza.fruitsappbackend.modulus.user.service_impl;

import com.hamza.fruitsappbackend.modulus.cart.entity.Cart;
import com.hamza.fruitsappbackend.modulus.cart.entity.CartItem;
import com.hamza.fruitsappbackend.modulus.order.entity.Order;
import com.hamza.fruitsappbackend.modulus.review.entity.Review;
import com.hamza.fruitsappbackend.modulus.role.entity.Role;
import com.hamza.fruitsappbackend.modulus.user.dto.UserDTO;
import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modulus.user.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.modulus.role.exception.RoleNotFoundException;
import com.hamza.fruitsappbackend.modulus.user.entity.Address;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import com.hamza.fruitsappbackend.modulus.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modulus.role.repository.RoleRepository;
import com.hamza.fruitsappbackend.modulus.user.service.UserService;
import com.hamza.fruitsappbackend.modulus.user.service.AccountVerificationService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthorizationUtils authorizationUtils;
    private final AccountVerificationService accountVerificationService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           ModelMapper modelMapper, PasswordEncoder passwordEncoder,
                           AuthorizationUtils authorizationUtils, AccountVerificationService accountVerificationService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.authorizationUtils = authorizationUtils;
        this.accountVerificationService = accountVerificationService;
    }

    @Override
    @Transactional
    public UserDTO addUser(UserDTO userDTO) {
        if (logger.isInfoEnabled()) {
            logger.info("Saving user with email: {}", userDTO.getEmail());
        }

        if (userDTO.getRoles() == null || userDTO.getRoles().isEmpty()) {
            throw new BadRequestException("At least one role is required.");
        }

        Set<String> roleNames = userDTO.getRoles().stream()
                .map(roleDto -> "ROLE_" + roleDto.getName().toUpperCase())
                .collect(Collectors.toSet());

        Set<Role> roles = roleRepository.findByNameIn(roleNames);

        if (roles.size() != roleNames.size()) {
            throw new RoleNotFoundException("Some roles not found.", String.valueOf(roleNames.size() - roles.size()));
        }

        User user = modelMapper.map(userDTO, User.class);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsVerified(false);
        user.setRoles(roles);

        mapAndSetRelatedEntities(userDTO, user);

        User savedUser = userRepository.save(user);

        if (logger.isInfoEnabled()) {
            logger.info("User saved successfully with email: {}", savedUser.getEmail());
        }

        return modelMapper.map(savedUser, UserDTO.class);
    }

    @Override
    public Optional<UserDTO> getUserById(Long id, String token) {
        authorizationUtils.checkUserOrAdminRole(token, id);
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserDTO.class))
                .or(() -> {
                    logger.error("User not found with ID: {}", id);
                    throw new UserNotFoundException("id", id.toString());
                });
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email, String token) {
        authorizationUtils.checkUserOrAdminRoleByEmail(token, email);

        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserDTO.class));
    }

    @Override
    public List<UserDTO> getAllUsers(String token) {
        authorizationUtils.checkAdminRole(token);

        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserDTO updateUser(UserDTO userDTO, String token) {
        authorizationUtils.checkUserOrAdminRole(token, userDTO.getId());

        User existingUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new UserNotFoundException("id", userDTO.getId().toString()));

        updateUserDetails(userDTO, existingUser);

        mapAndSetRelatedEntities(userDTO, existingUser);
        User updatedUser = userRepository.save(existingUser);

        logger.info("User updated successfully with ID: {}", userDTO.getId());
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public void deleteUserById(Long id, String token) {
        authorizationUtils.checkUserOrAdminRole(token, id);

        if (!userRepository.existsById(id)) {
            logger.error("User not found with ID: {}", id);
            throw new UserNotFoundException("id", id.toString());
        }

        userRepository.deleteById(id);
        logger.info("User deleted with ID: {}", id);
    }

    @Override
    public void deleteUserByEmail(String email, String token) {
        authorizationUtils.checkUserOrAdminRoleByEmail(token, email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));

        userRepository.delete(user);
        logger.info("User deleted with email: {}", email);
    }

    @Override
    public void sendVerificationEmail(String email) {
        accountVerificationService.sendVerificationEmail(email);
        logger.info("Verification email sent to: {}", email);
    }

    @Override
    public String verifyAccount(Integer otp) {
        String result = accountVerificationService.verifyAccount(otp);
        if ("Account verified successfully.".equals(result)) {
            User user = userRepository.findByOtp(otp)
                    .orElseThrow(() -> new UserNotFoundException("OTP", otp.toString()));
            user.setIsVerified(true);
            userRepository.save(user);
            logger.info("User verified successfully with OTP: {}", otp);
        }
        return result;
    }

    @Override
    public UserDTO getUserByOtp(Integer otp) {
        User user = userRepository.findByOtp(otp)
                .orElseThrow(() -> new UserNotFoundException("OTP", otp.toString()));
        return modelMapper.map(user, UserDTO.class);
    }

    private void updateUserDetails(UserDTO userDTO, User user) {
        if (userDTO.getName() != null) user.setName(userDTO.getName());
        if (userDTO.getEmail() != null) user.setEmail(userDTO.getEmail());
        if (userDTO.getPassword() != null) user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        if (userDTO.getImageUrl() != null) user.setImageUrl(userDTO.getImageUrl());
        if (userDTO.getIsVerified() != null) user.setIsVerified(userDTO.getIsVerified());

        if (userDTO.getRoles() != null) {
            Set<Role> roles = userDTO.getRoles().stream()
                    .map(roleDto -> roleRepository.findByName(roleDto.getName())
                            .orElseThrow(() -> new RoleNotFoundException("name", roleDto.getName())))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
    }

    private void mapAndSetRelatedEntities(UserDTO userDTO, User user) {
        // Map and set the list of addresses
        if (userDTO.getAddresses() != null) {
            user.setAddresses(
                    userDTO.getAddresses().stream()
                            .map(addressDTO -> modelMapper.map(addressDTO, Address.class))
                            .collect(Collectors.toList())
            );
        }

        // Map and set the list of carts (updated for one-to-many relationship)
        if (userDTO.getCarts() != null) {  // Use getCarts() for multiple carts
            user.setCarts(
                    userDTO.getCarts().stream()  // Convert each CartDTO to Cart entity
                            .map(cartDTO -> modelMapper.map(cartDTO, Cart.class))
                            .collect(Collectors.toList())
            );
        }

        // Map and set the list of orders
        if (userDTO.getOrders() != null) {
            user.setOrders(
                    userDTO.getOrders().stream()
                            .map(orderDTO -> modelMapper.map(orderDTO, Order.class))
                            .collect(Collectors.toList())
            );
        }

        // Map and set the list of reviews
        if (userDTO.getReviews() != null) {
            user.setReviews(
                    userDTO.getReviews().stream()
                            .map(reviewDTO -> modelMapper.map(reviewDTO, Review.class))
                            .collect(Collectors.toList())
            );
        }
    }



    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities(user.getRoles().stream()
                        .map(role -> "ROLE_" + role.getName())
                        .toList()
                        .toArray(new String[0]))
                .build();
    }
}
