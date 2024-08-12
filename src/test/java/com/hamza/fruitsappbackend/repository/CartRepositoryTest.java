package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.entity.Cart;
import com.hamza.fruitsappbackend.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private UserRepository userRepository;

    private Cart cart;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setName("John Doe");
        user.setEmail("johndoe@example.com");
        user.setPasswordHash("hashedpassword");
        userRepository.save(user);

        cart = new Cart();
        cart.setUser(user);
    }

    @Test
    void testSaveCart() {
        Cart savedCart = cartRepository.save(cart);
        assertNotNull(savedCart.getId());
    }

    @Test
    void testFindCartById() {
        Cart savedCart = cartRepository.save(cart);
        Optional<Cart> retrievedCart = cartRepository.findById(savedCart.getId());
        assertTrue(retrievedCart.isPresent());
        assertEquals(savedCart.getId(), retrievedCart.get().getId());
    }

    @Test
    void testDeleteCart() {
        Cart savedCart = cartRepository.save(cart);
        cartRepository.deleteById(savedCart.getId());
        Optional<Cart> deletedCart = cartRepository.findById(savedCart.getId());
        assertFalse(deletedCart.isPresent());
    }

    @Test
    void testUpdateCart() {
        Cart savedCart = cartRepository.save(cart);
        savedCart.setUser(user);
        Cart updatedCart = cartRepository.save(savedCart);
        assertEquals(user.getId(), updatedCart.getUser().getId());
    }
}
