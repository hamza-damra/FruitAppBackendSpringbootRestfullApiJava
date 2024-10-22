package com.hamza.fruitsappbackend.modulus.order.service_impl;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.modulus.cart.entity.Cart;
import com.hamza.fruitsappbackend.modulus.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modulus.order.dto.OrderDTO;
import com.hamza.fruitsappbackend.modulus.order.dto.OrderResponseDto;
import com.hamza.fruitsappbackend.modulus.order.entity.Order;
import com.hamza.fruitsappbackend.modulus.order.entity.OrderItem;
import com.hamza.fruitsappbackend.modulus.user.entity.Address;
import com.hamza.fruitsappbackend.modulus.user.entity.User;
import com.hamza.fruitsappbackend.modulus.order.exception.OrderNotFoundException;
import com.hamza.fruitsappbackend.modulus.order.repository.OrderRepository;
import com.hamza.fruitsappbackend.modulus.user.repository.UserRepository;
import com.hamza.fruitsappbackend.modulus.order.service.OrderService;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.modulus.product.entity.Product;
import com.hamza.fruitsappbackend.modulus.product.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository,
                            ProductRepository productRepository, CartRepository cartRepository,
                            ModelMapper modelMapper, AuthorizationUtils authorizationUtils) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO, String token) {

        // Get the user ID from the token and check authorization
        Long userId = authorizationUtils.getUserFromToken(token).getId();
        authorizationUtils.checkUserOrAdminRole(token, userId);

        // Fetch the user and their active cart
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderNotFoundException("userId", userId.toString()));

        List<Cart> activeCarts = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE);

        if (activeCarts.size() > 1) {
            throw new IllegalStateException("Multiple active carts found for user. Data inconsistency detected.");
        } else if (activeCarts.isEmpty()) {
            throw new IllegalStateException("No active cart found for the user (Add product to your cart)");
        }

        Cart cart = activeCarts.get(0);

        // Create the order and set initial values
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(cart.getTotalPrice());
        order.setCart(cart);  // Link the cart to the order (many-to-one relationship)

        // Set the default address for the order
        order.setAddress(
                user.getAddresses().stream()
                        .filter(Address::isDefault)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No default address found for the user"))
        );

        // Move CartItems to OrderItems
        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setOrder(order);  // Associate OrderItem with the Order

                    // Increment the product's order count
                    Product product = cartItem.getProduct();
                    product.setOrderCount(product.getOrderCount() + 1);

                    return orderItem;
                }).collect(Collectors.toList());

        // Set the order items in the order
        order.setOrderItems(orderItems);

        // Save the order
        Order savedOrder = orderRepository.save(order);

        // Mark the cart as completed and save the updated cart
        cart.completeCart();  // Mark the cart as completed
        cartRepository.saveAndFlush(cart);

        // Create a new cart for future orders (since the old cart is now completed)
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setTotalPrice(BigDecimal.ZERO);
        newCart.setTotalQuantity(0);  // Initialize total quantity
        newCart.setStatus(CartStatus.ACTIVE);


        cartRepository.save(newCart);

        // Return the mapped Order DTO
        return modelMapper.map(savedOrder, OrderDTO.class);
    }




    @Override
    @Cacheable(value = "orders", key = "#id")
    public Optional<OrderDTO> getOrderById(Long id, String token) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, order.getUser().getId());
        return Optional.of(modelMapper.map(order, OrderDTO.class));
    }

    @Override
    public OrderResponseDto getOrdersByUserId(String token) {
        // Get the user from the token
        User user = authorizationUtils.getUserFromToken(token);
        Long userId = user.getId();

        // Check user role authorization
        authorizationUtils.checkUserOrAdminRole(token, userId);

        // Retrieve orders for the user
        List<Order> userOrders = orderRepository.findByUserId(userId);
        List<OrderDTO> userOrdersDto = userOrders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();

        // Count the total number of orders
        // Calculate total price of all orders
        BigDecimal totalPrice = userOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return the response with total price, orders count, and the list of orders
        return new OrderResponseDto(totalPrice, userOrders.size(), userOrdersDto);
    }



    @Override
    public List<OrderDTO> getAllOrders(String token) {
        authorizationUtils.checkAdminRole(token);

        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus, String token) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId.toString()));

        authorizationUtils.checkUserOrAdminRole(token, order.getUser().getId());

        if (isTransitionValid(order.getStatus(), newStatus)) {
            handleOrderStatusChange(order, newStatus);
            Order updatedOrder = orderRepository.save(order);
            return modelMapper.map(updatedOrder, OrderDTO.class);
        } else {
            throw new IllegalStateException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }
    }

    @Override
    public void deleteOrderById(Long id, String token) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("id", id.toString()));

        authorizationUtils.checkUserOrAdminRole(token, order.getUser().getId());
        orderRepository.deleteById(id);
    }

    @Transactional
    protected void handleOrderStatusChange(Order order, OrderStatus newStatus) {
        if (newStatus == OrderStatus.FAILED || newStatus == OrderStatus.RETURNED || newStatus == OrderStatus.CANCELLED) {
            order.getOrderItems().forEach(orderItem -> {
                Product product = orderItem.getProduct();
                product.setOrderCount(product.getOrderCount() - 1);
                productRepository.save(product);
            });
        }
        order.setStatus(newStatus);
    }

    private boolean isTransitionValid(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.FAILED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.RETURNED;
            case DELIVERED, CANCELLED, RETURNED, FAILED -> false;
        };
    }

    @Override
    @Transactional
    public OrderDTO updateOrderByUserIdAndOrderId(Long orderId, Long userId, OrderDTO orderDTO, String token) {
        // Check user authorization (either admin or the actual user)
        authorizationUtils.checkUserOrAdminRole(token, userId);

        // Fetch the order from the database
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId.toString()));

        // Ensure the order belongs to the user
        if (!existingOrder.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to the specified user");
        }

        // Save previous status for transition checks
        OrderStatus previousStatus = existingOrder.getStatus();

        // Map updated DTO values into the existing order entity
        modelMapper.map(orderDTO, existingOrder);

        // Ensure valid status transition
        if (isTransitionValid(previousStatus, orderDTO.getStatus())) {
            if (orderDTO.getStatus() == OrderStatus.FAILED || orderDTO.getStatus() == OrderStatus.RETURNED || orderDTO.getStatus() == OrderStatus.CANCELLED) {
                // Revert product order count if the order fails, is returned, or canceled
                existingOrder.getOrderItems().forEach(orderItem -> {
                    Product product = orderItem.getProduct();
                    product.setOrderCount(product.getOrderCount() - 1);
                    productRepository.save(product);
                });
            }
            existingOrder.setStatus(orderDTO.getStatus());
        } else {
            throw new IllegalStateException("Invalid status transition from " + previousStatus + " to " + orderDTO.getStatus());
        }

        // Save the updated order back into the database
        Order updatedOrder = orderRepository.save(existingOrder);
        return modelMapper.map(updatedOrder, OrderDTO.class);
    }

    @Override
    @Transactional
    public void deleteOrdersByUserId(Long userId, String token) {
        // Check that the token belongs to an admin
        authorizationUtils.checkAdminRole(token);

        // Retrieve the user to ensure existence
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderNotFoundException("User", userId.toString()));

        // Delete all orders by the user
        orderRepository.deleteByUser(user);
    }

    @Override
    @Transactional
    public void deleteOrderByIdAndUserId(Long orderId, Long userId, String token) {
        // Check that the user is authorized (either admin or the user themselves)
        authorizationUtils.checkUserOrAdminRole(token, userId);

        // Ensure the order exists and belongs to the user
        if (orderRepository.existsByIdAndUserId(orderId, userId)) {
            orderRepository.deleteByIdAndUserId(orderId, userId);
        } else {
            throw new OrderNotFoundException("Order", orderId.toString());
        }
    }

}
