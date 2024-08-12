package com.hamza.fruitsappbackend.service;

import com.hamza.fruitsappbackend.dto.AddressDTO;
import com.hamza.fruitsappbackend.dto.UserDTO;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.entity.User;
import com.hamza.fruitsappbackend.repository.UserRepository;
import com.hamza.fruitsappbackend.service.service_impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDTO userDTO;
    private Address address;
    private AddressDTO addressDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Setup for User and related entities
        address = new Address();
        address.setId(1L);
        address.setCity("New York");
        address.setStreetAddress("123 Main St");

        user = new User();
        user.setId(1L);
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setPasswordHash("hashedpassword");
        user.setAddresses(Arrays.asList(address));

        addressDTO = new AddressDTO();
        addressDTO.setId(1L);
        addressDTO.setCity("New York");
        addressDTO.setStreetAddress("123 Main St");

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setName("John Doe");
        userDTO.setEmail("johndoe@example.com");
        userDTO.setPasswordHash("hashedpassword");
        userDTO.setAddresses(Arrays.asList(addressDTO));
    }

    @Test
    void saveUser() {
        // Mock the mapping and save operations
        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);
        when(modelMapper.map(addressDTO, Address.class)).thenReturn(address);
        when(modelMapper.map(address, AddressDTO.class)).thenReturn(addressDTO);

        // Call the service method
        UserDTO savedUserDTO = userService.saveUser(userDTO);

        // Verify the results
        assertNotNull(savedUserDTO);
        assertEquals(userDTO.getId(), savedUserDTO.getId());
        assertEquals(userDTO.getName(), savedUserDTO.getName());

        // Verify related entities were processed correctly
        assertEquals(1, savedUserDTO.getAddresses().size());
        assertEquals("New York", savedUserDTO.getAddresses().get(0).getCity());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUser() {
        // Mock existing user retrieval and mapping
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(userDTO, User.class)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);
        when(modelMapper.map(addressDTO, Address.class)).thenReturn(address);
        when(modelMapper.map(address, AddressDTO.class)).thenReturn(addressDTO);

        // Call the service method
        UserDTO updatedUserDTO = userService.updateUser(userDTO);

        // Verify the results
        assertNotNull(updatedUserDTO);
        assertEquals(userDTO.getId(), updatedUserDTO.getId());
        assertEquals(userDTO.getName(), updatedUserDTO.getName());

        // Verify related entities were processed correctly
        assertEquals(1, updatedUserDTO.getAddresses().size());
        assertEquals("New York", updatedUserDTO.getAddresses().get(0).getCity());

        verify(userRepository, times(1)).save(user);
    }

    @Test
    void getUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        Optional<UserDTO> foundUserDTO = userService.getUserById(1L);

        assertTrue(foundUserDTO.isPresent());
        assertEquals(userDTO.getId(), foundUserDTO.get().getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void getUserByEmail() {
        when(userRepository.findByEmail("johndoe@example.com")).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);

        Optional<UserDTO> foundUserDTO = userService.getUserByEmail("johndoe@example.com");

        assertTrue(foundUserDTO.isPresent());
        assertEquals(userDTO.getEmail(), foundUserDTO.get().getEmail());
        verify(userRepository, times(1)).findByEmail("johndoe@example.com");
    }

    @Test
    void getAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setName("Jane Doe");
        user2.setEmail("janedoe@example.com");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setName("Jane Doe");
        userDTO2.setEmail("janedoe@example.com");

        when(userRepository.findAll()).thenReturn(Arrays.asList(user, user2));
        when(modelMapper.map(user, UserDTO.class)).thenReturn(userDTO);
        when(modelMapper.map(user2, UserDTO.class)).thenReturn(userDTO2);

        List<UserDTO> allUsers = userService.getAllUsers();

        assertEquals(2, allUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUserById() {
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUserById(1L);

        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUserByEmail() {
        when(userRepository.findByEmail("johndoe@example.com")).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);

        userService.deleteUserByEmail("johndoe@example.com");

        verify(userRepository, times(1)).findByEmail("johndoe@example.com");
        verify(userRepository, times(1)).delete(user);
    }
}
