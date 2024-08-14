package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.entity.User;
import jakarta.persistence.EntityManager;
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
class AddressRepositoryTest {

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private Address address;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setPassword("hashedpassword");
        userRepository.save(user);

        address = new Address();
        address.setFullName("John Doe");
        address.setEmail("johndoe@example.com");
        address.setCity("New York");
        address.setStreetAddress("123 Main St");
        address.setUser(user);
    }

    @Test
    void testSaveAddress() {
        Address savedAddress = addressRepository.save(address);
        assertNotNull(savedAddress.getId());
        assertEquals("John Doe", savedAddress.getFullName());
        assertEquals("123 Main St", savedAddress.getStreetAddress());
    }

    @Test
    void testFindAddressById() {
        Address savedAddress = addressRepository.save(address);
        Optional<Address> retrievedAddress = addressRepository.findById(savedAddress.getId());
        assertTrue(retrievedAddress.isPresent());
        assertEquals(savedAddress.getId(), retrievedAddress.get().getId());
    }

    @Test
    void testUpdateAddress() {
        Address savedAddress = addressRepository.save(address);
        savedAddress.setCity("San Francisco");
        Address updatedAddress = addressRepository.save(savedAddress);
        assertEquals("San Francisco", updatedAddress.getCity());
    }

    @Test
    void testDeleteAddress() {
        Address savedAddress = addressRepository.save(address);
        addressRepository.deleteById(savedAddress.getId());
        Optional<Address> deletedAddress = addressRepository.findById(savedAddress.getId());
        assertFalse(deletedAddress.isPresent());
    }

    @Test
    void testUniqueConstraintViolation() {
        addressRepository.save(address);
        entityManager.flush();  // Ensure the first insert is flushed to the database

        Address anotherAddress = new Address();
        anotherAddress.setFullName("Jane Doe");
        anotherAddress.setEmail("johndoe@example.com"); // Same email as the first address
        anotherAddress.setCity("Los Angeles");
        anotherAddress.setStreetAddress("456 Another St");
        anotherAddress.setUser(user);

        assertThrows(DataIntegrityViolationException.class, () -> {
            addressRepository.save(anotherAddress);
            entityManager.flush();  // Trigger the constraint violation by flushing
        });
    }

}
