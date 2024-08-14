package com.hamza.fruitsappbackend.repository;

import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.constant.PaymentMethod;
import com.hamza.fruitsappbackend.entity.Address;
import com.hamza.fruitsappbackend.entity.Order;
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
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    private Order order;
    private User user;
    private Address address;

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
        addressRepository.save(address);

        order = new Order();
        order.setTotalPrice(100.0);
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentMethod(PaymentMethod.CASH_ON_DELIVERY);
        order.setUser(user);
        order.setAddress(address);
    }

    @Test
    void testSaveOrder() {
        Order savedOrder = orderRepository.save(order);
        assertNotNull(savedOrder.getId());
    }

    @Test
    void testFindOrderById() {
        Order savedOrder = orderRepository.save(order);
        Optional<Order> retrievedOrder = orderRepository.findById(savedOrder.getId());
        assertTrue(retrievedOrder.isPresent());
        assertEquals(savedOrder.getId(), retrievedOrder.get().getId());
    }

    @Test
    void testUpdateOrder() {
        Order savedOrder = orderRepository.save(order);
        savedOrder.setTotalPrice(200.0);
        Order updatedOrder = orderRepository.save(savedOrder);
        assertEquals(200.0, updatedOrder.getTotalPrice());
    }

    @Test
    void testDeleteOrder() {
        Order savedOrder = orderRepository.save(order);
        orderRepository.deleteById(savedOrder.getId());
        Optional<Order> deletedOrder = orderRepository.findById(savedOrder.getId());
        assertFalse(deletedOrder.isPresent());
    }
}
