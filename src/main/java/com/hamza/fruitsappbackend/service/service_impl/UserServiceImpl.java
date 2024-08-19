package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.UserDTO;
import com.hamza.fruitsappbackend.entity.*;
import com.hamza.fruitsappbackend.exception.UserNotFoundException;
import com.hamza.fruitsappbackend.exception.RoleNotFoundException;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.repository.RoleRepository;
import com.hamza.fruitsappbackend.service.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository,
                           ModelMapper modelMapper, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO saveUser(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }

        if (userDTO.getRoles() != null && !userDTO.getRoles().isEmpty()) {
            Set<Role> roles = userDTO.getRoles().stream()
                    .map(roleDto -> roleRepository.findByName("ROLE_" + roleDto.getName().toUpperCase())
                            .orElseThrow(() -> new RoleNotFoundException("name", roleDto.getName())))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        mapAndSetRelatedEntities(userDTO, user);
        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDTO.class);
    }


    @Override
    public Optional<UserDTO> getUserById(Long id) {
        return userRepository.findById(id)
                .map(user -> modelMapper.map(user, UserDTO.class))
                .or(() -> {
                    throw new UserNotFoundException("id", id.toString());
                });
    }

    @Override
    public Optional<UserDTO> getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(user -> modelMapper.map(user, UserDTO.class))
                .or(() -> {
                    throw new UserNotFoundException("email", email);
                });
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO updateUser(UserDTO userDTO) {
        User existingUser = userRepository.findById(userDTO.getId())
                .orElseThrow(() -> new UserNotFoundException("id", userDTO.getId().toString()));

        if (userDTO.getName() != null) {
            existingUser.setName(userDTO.getName());
        }
        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }

        if (userDTO.getRoles() != null) {
            Set<Role> roles = userDTO.getRoles().stream()
                    .map(roleDto -> roleRepository.findByName(roleDto.getName())
                            .orElseThrow(() -> new RoleNotFoundException("name", roleDto.getName())))
                    .collect(Collectors.toSet());
            existingUser.setRoles(roles);
        }

        mapAndSetRelatedEntities(userDTO, existingUser);

        User updatedUser = userRepository.save(existingUser);
        return modelMapper.map(updatedUser, UserDTO.class);
    }

    @Override
    public void deleteUserById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("id", id.toString());
        }
        userRepository.deleteById(id);
    }

    @Override
    public void deleteUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("email", email));
        userRepository.delete(user);
    }

    private void mapAndSetRelatedEntities(UserDTO userDTO, User user) {
        if (userDTO.getAddresses() != null) {
            user.setAddresses(
                    userDTO.getAddresses().stream()
                            .map(addressDTO -> modelMapper.map(addressDTO, Address.class))
                            .collect(Collectors.toList())
            );
        }

        if (userDTO.getCart() != null) {
            user.setCart(modelMapper.map(userDTO.getCart(), Cart.class));
        }

        if (userDTO.getOrders() != null) {
            user.setOrders(
                    userDTO.getOrders().stream()
                            .map(orderDTO -> modelMapper.map(orderDTO, Order.class))
                            .collect(Collectors.toList())
            );
        }

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
