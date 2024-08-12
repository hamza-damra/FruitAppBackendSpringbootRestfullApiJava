package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        // Initialize a User object before each test
        user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setPasswordHash("hashedpassword");
    }

    @Test
    void testSaveUser() {
        User savedUser = userRepository.save(user);
        assertNotNull(savedUser.getId());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("johndoe@example.com", savedUser.getEmail());
        assertEquals("hashedpassword", savedUser.getPasswordHash());
    }

    @Test
    void testFindUserById() {
        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Retrieve the user by its ID
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());

        // Verify that the user was retrieved correctly
        assertTrue(retrievedUser.isPresent());
        assertEquals(savedUser.getId(), retrievedUser.get().getId());
        assertEquals("John Doe", retrievedUser.get().getName());
    }

    @Test
    void testFindUserByEmail() {
        // Save the user to the database
        userRepository.save(user);

        // Retrieve the user by its email
        Optional<User> retrievedUser = userRepository.findByEmail(user.getEmail());

        // Verify that the user was retrieved correctly
        assertTrue(retrievedUser.isPresent());
        assertEquals(user.getEmail(), retrievedUser.get().getEmail());
    }

    @Test
    void testDeleteUser() {
        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Delete the user by its ID
        userRepository.deleteById(savedUser.getId());

        // Verify that the user was deleted
        Optional<User> deletedUser = userRepository.findById(savedUser.getId());
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void testUpdateUser() {
        // Save the user to the database
        User savedUser = userRepository.save(user);

        // Update the user's name
        savedUser.setName("Jane Doe");
        User updatedUser = userRepository.save(savedUser);

        // Verify that the user's name was updated correctly
        assertEquals("Jane Doe", updatedUser.getName());

        // Verify that the user's other attributes remain unchanged
        assertEquals("johndoe@example.com", updatedUser.getEmail());
    }

    @Test
    void testEmailUniqueness() {
        // Save the first user to the database
        userRepository.save(user);

        // Attempt to save another user with the same email
        User anotherUser = new User();
        anotherUser.setName("Jane Smith");
        anotherUser.setEmail("johndoe@example.com"); // Same email as the first user
        anotherUser.setPasswordHash("anotherhashedpassword");

        assertThrows(DataIntegrityViolationException.class, () -> {
            userRepository.save(anotherUser);
        });
    }

    @Test
    void testFindByIdNotFound() {
        // Try to retrieve a user that does not exist
        Optional<User> retrievedUser = userRepository.findById(999L);

        // Verify that no user was found
        assertFalse(retrievedUser.isPresent());
    }

    @Test
    void testFindByEmailNotFound() {
        // Try to retrieve a user by an email that does not exist
        Optional<User> retrievedUser = userRepository.findByEmail("nonexistent@example.com");

        // Verify that no user was found
        assertFalse(retrievedUser.isPresent());
    }

    @Test
    void testDeleteUserByEmail() {
        // Save the user to the database
        userRepository.save(user);

        // Delete the user by its email
        Optional<User> retrievedUser = userRepository.findByEmail(user.getEmail());
        assertTrue(retrievedUser.isPresent());

        userRepository.delete(retrievedUser.get());

        // Verify that the user was deleted
        Optional<User> deletedUser = userRepository.findByEmail(user.getEmail());
        assertFalse(deletedUser.isPresent());
    }
}
