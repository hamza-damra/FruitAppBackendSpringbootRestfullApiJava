package com.hamza.fruitsappbackend.modules.order.service_impl;

import com.hamza.fruitsappbackend.constant.CartStatus;
import com.hamza.fruitsappbackend.constant.OrderStatus;
import com.hamza.fruitsappbackend.exception.global.BadRequestException;
import com.hamza.fruitsappbackend.modules.cart.entity.Cart;
import com.hamza.fruitsappbackend.modules.cart.entity.CartItem;
import com.hamza.fruitsappbackend.modules.cart.repository.CartRepository;
import com.hamza.fruitsappbackend.modules.order.dto.OrderDTO;
import com.hamza.fruitsappbackend.modules.order.dto.OrderResponseDto;
import com.hamza.fruitsappbackend.modules.order.entity.Order;
import com.hamza.fruitsappbackend.modules.order.entity.OrderItem;
import com.hamza.fruitsappbackend.modules.order.exception.OrderNotFoundException;
import com.hamza.fruitsappbackend.modules.order.repository.OrderRepository;
import com.hamza.fruitsappbackend.modules.order.service.OrderService;
import com.hamza.fruitsappbackend.modules.product.entity.Product;
import com.hamza.fruitsappbackend.modules.product.repository.ProductRepository;
import com.hamza.fruitsappbackend.modules.user.entity.Address;
import com.hamza.fruitsappbackend.modules.user.entity.User;
import com.hamza.fruitsappbackend.modules.user.repository.UserRepository;
import com.hamza.fruitsappbackend.utils.AuthorizationUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

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
    private final PlatformTransactionManager transactionManager;
    private final ModelMapper modelMapper;
    private final AuthorizationUtils authorizationUtils;

    @Autowired
    public OrderServiceImpl(OrderRepository orderRepository, UserRepository userRepository,
                            ProductRepository productRepository, CartRepository cartRepository,
                            ModelMapper modelMapper,PlatformTransactionManager transactionManager, AuthorizationUtils authorizationUtils) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.transactionManager = transactionManager;
        this.modelMapper = modelMapper;
        this.authorizationUtils = authorizationUtils;
    }

    @Override
    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO, String token) {
        Long userId = authorizeUser(token);
        User user = fetchUserById(userId);
        Cart cart = fetchActiveCartForUser(userId);
        validateCartNotEmpty(cart);

        Order order = createOrderFromCart(user, cart);
        Order savedOrder = orderRepository.save(order);

        completeCart(cart);
        createNewActiveCartForUser(user);

        return modelMapper.map(savedOrder, OrderDTO.class);
    }

    @Override
    @Cacheable(value = "orders", key = "#id")
    public Optional<OrderDTO> getOrderById(Long id, String token) {
        Order order = fetchOrderById(id);
        authorizeUserAccess(token, order.getUser().getId());
        return Optional.of(modelMapper.map(order, OrderDTO.class));
    }

    @Override
    public OrderResponseDto getOrdersByUserId(String token) {
        Long userId = authorizeUser(token);
        List<Order> userOrders = orderRepository.findByUserId(userId);

        return buildOrderResponseDto(userOrders);
    }

    @Override
    public List<OrderDTO> getAllOrders(String token) {
        authorizeAdmin(token);
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus newStatus, String token) {
        Order order = fetchOrderById(orderId);
        authorizeUserAccess(token, order.getUser().getId());

        validateAndHandleOrderStatusTransition(order, newStatus);
        return modelMapper.map(orderRepository.save(order), OrderDTO.class);
    }

    @Override
    public void deleteOrderById(Long id, String token) {
        Order order = fetchOrderById(id);
        authorizeUserAccess(token, order.getUser().getId());
        orderRepository.deleteById(id);
    }

    @Transactional
    public void deleteOrdersByUserToken(String token) {
        authorizeAdmin(token);
        Long userId = authorizeUser(token);
        orderRepository.deleteByUser(fetchUserById(userId));
    }

    @Transactional
    public void deleteOrderByIdAndUserToken(Long orderId, String token) {
        Long userId = authorizeUser(token);
        authorizeUserAccess(token, userId);

        if (!orderRepository.existsByIdAndUserId(orderId, userId)) {
            throw new OrderNotFoundException("Order", orderId.toString());
        }
        orderRepository.deleteByIdAndUserId(orderId, userId);
    }

    @Override
    @Transactional
    public OrderDTO updateOrderByUserTokenAndOrderId(Long orderId, OrderDTO orderDTO, String token) {
        Long userId = authorizeUser(token);
        Order existingOrder = fetchOrderById(orderId);

        if (!existingOrder.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to the specified user");
        }

        updateOrderStatusIfValid(existingOrder, orderDTO.getStatus());
        modelMapper.map(orderDTO, existingOrder);
        return modelMapper.map(orderRepository.save(existingOrder), OrderDTO.class);
    }



    private Long authorizeUser(String token) {
        return authorizationUtils.getUserFromToken(token).getId();
    }

    private void authorizeAdmin(String token) {
        authorizationUtils.checkAdminRole(token);
    }

    private void authorizeUserAccess(String token, Long userId) {
        authorizationUtils.checkUserOrAdminRole(token, userId);
    }

    private User fetchUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new OrderNotFoundException("userId", userId.toString()));
    }

    private Order fetchOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId.toString()));
    }

    private Cart fetchActiveCartForUser(Long userId) {
        List<Cart> activeCarts = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE);
        if (activeCarts.size() > 1) {
            throw new IllegalStateException("Multiple active carts found for user. Data inconsistency detected.");
        } else if (activeCarts.isEmpty()) {
            throw new IllegalStateException("No active cart found for the user (Add product to your cart)");
        }
        return activeCarts.get(0);
    }

    private void validateCartNotEmpty(Cart cart) {
        if (cart.getCartItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Please add products to your cart.");
        }
    }




    private Order createOrderFromCart(User user, Cart cart) {
        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(cart.getTotalPrice());
        order.setCart(cart);
        order.setAddress(fetchDefaultAddress(user));

        List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> createOrderItemFromCartItem(cartItem, order))
                .collect(Collectors.toList());

        order.setOrderItems(orderItems);

        return order;
    }


    private Address fetchDefaultAddress(User user) {
        return user.getAddresses().stream()
                .filter(Address::isDefault)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No default address found for the user."));
    }

    private OrderItem createOrderItemFromCartItem(CartItem cartItem, Order order) {
        Product product = cartItem.getProduct();
        if (product.getStockQuantity() < cartItem.getQuantity()) {
            throw new IllegalStateException("Insufficient stock for product: " + product.getName());
        }

        product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
        product.setOrderCount(product.getOrderCount() + 1);

        OrderItem orderItem = new OrderItem();
        orderItem.setProduct(product);
        orderItem.setQuantity(cartItem.getQuantity());
        orderItem.setPrice(cartItem.getPrice());
        orderItem.setOrder(order);

        return orderItem;
    }


    private void completeCart(Cart cart) {
        cart.completeCart();
        cartRepository.saveAndFlush(cart);
    }

    private void createNewActiveCartForUser(User user) {
        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setTotalPrice(BigDecimal.ZERO);
        newCart.setTotalQuantity(0);
        newCart.setStatus(CartStatus.ACTIVE);
        cartRepository.save(newCart);
    }

    private void validateAndHandleOrderStatusTransition(Order order, OrderStatus newStatus) {
        if (isTransitionValid(order.getStatus(), newStatus)) {
            handleOrderStatusChange(order, newStatus);
        } else {
            throw new IllegalStateException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }
    }

    private boolean isTransitionValid(OrderStatus currentStatus, OrderStatus newStatus) {
        return switch (currentStatus) {
            case PENDING -> newStatus == OrderStatus.PROCESSING || newStatus == OrderStatus.CANCELLED;
            case PROCESSING -> newStatus == OrderStatus.SHIPPED || newStatus == OrderStatus.FAILED;
            case SHIPPED -> newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.RETURNED;
            default -> false;
        };
    }



    public void handleOrderStatusChange(Order order, OrderStatus newStatus) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {

            if (newStatus == OrderStatus.FAILED || newStatus == OrderStatus.RETURNED || newStatus == OrderStatus.CANCELLED) {
                order.getOrderItems().forEach(orderItem -> {
                    Product product = orderItem.getProduct();
                    product.setOrderCount(product.getOrderCount() - 1);
                    productRepository.save(product);
                });
            }
            order.setStatus(newStatus);
            transactionManager.commit(status);
        } catch (RuntimeException e) {
            transactionManager.rollback(status);
            throw e;
        }
    }


    private OrderResponseDto buildOrderResponseDto(List<Order> orders) {
        List<OrderDTO> orderDTOList = orders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .collect(Collectors.toList());

        BigDecimal totalPrice = orders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new OrderResponseDto(totalPrice, orders.size(), orderDTOList);
    }

    private void updateOrderStatusIfValid(Order order, OrderStatus newStatus) {
        if (isTransitionValid(order.getStatus(), newStatus)) {
            handleOrderStatusChange(order, newStatus);
        } else {
            throw new IllegalStateException("Invalid status transition from " + order.getStatus() + " to " + newStatus);
        }
    }
}
