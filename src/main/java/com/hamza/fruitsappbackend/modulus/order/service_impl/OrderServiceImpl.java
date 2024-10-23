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

        Long userId = authorizationUtils.getUserFromToken(token).getId();
        authorizationUtils.checkUserOrAdminRole(token, userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderNotFoundException("userId", userId.toString()));

        List<Cart> activeCarts = cartRepository.findAllByUserIdAndStatus(userId, CartStatus.ACTIVE);

        if (activeCarts.size() > 1) {
            throw new IllegalStateException("Multiple active carts found for user. Data inconsistency detected.");
        } else if (activeCarts.isEmpty()) {
            throw new IllegalStateException("No active cart found for the user (Add product to your cart)");
        }

        Cart cart = activeCarts.get(0);


        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(cart.getTotalPrice());
        order.setCart(cart);

        order.setAddress(
                user.getAddresses().stream()
                        .filter(Address::isDefault)
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("No default address found for the user"))
        );

           List<OrderItem> orderItems = cart.getCartItems().stream()
                .map(cartItem -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setProduct(cartItem.getProduct());
                    orderItem.setQuantity(cartItem.getQuantity());
                    orderItem.setPrice(cartItem.getPrice());
                    orderItem.setOrder(order);

                    Product product = cartItem.getProduct();
                    product.setOrderCount(product.getOrderCount() + 1);

                    return orderItem;
                }).collect(Collectors.toList());

        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);

        cart.completeCart();
        cartRepository.saveAndFlush(cart);

        Cart newCart = new Cart();
        newCart.setUser(user);
        newCart.setTotalPrice(BigDecimal.ZERO);
        newCart.setTotalQuantity(0);
        newCart.setStatus(CartStatus.ACTIVE);


        cartRepository.save(newCart);

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
        User user = authorizationUtils.getUserFromToken(token);
        Long userId = user.getId();

        authorizationUtils.checkUserOrAdminRole(token, userId);

        List<Order> userOrders = orderRepository.findByUserId(userId);
        List<OrderDTO> userOrdersDto = userOrders.stream()
                .map(order -> modelMapper.map(order, OrderDTO.class))
                .toList();

        BigDecimal totalPrice = userOrders.stream()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

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
    public OrderDTO updateOrderByUserTokenAndOrderId(Long orderId, OrderDTO orderDTO, String token) {

        Long userId = authorizationUtils.getUserFromToken(token).getId();

        authorizationUtils.checkUserOrAdminRole(token, userId);


        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("id", orderId.toString()));


        if (!existingOrder.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to the specified user");
        }


        OrderStatus previousStatus = existingOrder.getStatus();


        modelMapper.map(orderDTO, existingOrder);


        if (isTransitionValid(previousStatus, orderDTO.getStatus())) {
            if (orderDTO.getStatus() == OrderStatus.FAILED || orderDTO.getStatus() == OrderStatus.RETURNED || orderDTO.getStatus() == OrderStatus.CANCELLED) {
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

        Order updatedOrder = orderRepository.save(existingOrder);
        return modelMapper.map(updatedOrder, OrderDTO.class);
    }

    @Transactional
    public void deleteOrdersByUserToken(String token) {
        authorizationUtils.checkAdminRole(token);
        Long userId = authorizationUtils.getUserFromToken(token).getId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new OrderNotFoundException("User", userId.toString()));

        orderRepository.deleteByUser(user);
    }

    @Transactional
    public void deleteOrderByIdAndUserToken(Long orderId, String token) {
        Long userId = authorizationUtils.getUserFromToken(token).getId();
        authorizationUtils.checkUserOrAdminRole(token, userId);

        if (orderRepository.existsByIdAndUserId(orderId, userId)) {
            orderRepository.deleteByIdAndUserId(orderId, userId);
        } else {
            throw new OrderNotFoundException("Order", orderId.toString());
        }
    }

}
