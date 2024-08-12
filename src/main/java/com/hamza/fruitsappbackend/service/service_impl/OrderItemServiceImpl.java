package com.hamza.fruitsappbackend.service.service_impl;

import com.hamza.fruitsappbackend.dto.OrderItemDTO;
import com.hamza.fruitsappbackend.entity.OrderItem;
import com.hamza.fruitsappbackend.entity.Product;
import com.hamza.fruitsappbackend.repository.OrderItemRepository;
import com.hamza.fruitsappbackend.repository.ProductRepository;
import com.hamza.fruitsappbackend.service.OrderItemService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderItemServiceImpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public OrderItemServiceImpl(OrderItemRepository orderItemRepository, ProductRepository productRepository, ModelMapper modelMapper) {
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public OrderItemDTO saveOrderItem(OrderItemDTO orderItemDTO) {
        OrderItem orderItem = modelMapper.map(orderItemDTO, OrderItem.class);

        // Retrieve the Product and its price
        Optional<Product> productOptional = productRepository.findById(orderItem.getProduct().getId());
        if (productOptional.isPresent()) {
            Product product = productOptional.get();
            orderItem.setProduct(product); // Set the product in OrderItem
            orderItem.setPrice(product.getPrice()); // Set the price from the product
        } else {
            throw new RuntimeException("Product not found for the given ID.");
        }

        OrderItem savedOrderItem = orderItemRepository.save(orderItem);
        return modelMapper.map(savedOrderItem, OrderItemDTO.class);
    }

    @Override
    public Optional<OrderItemDTO> getOrderItemById(Long id) {
        return orderItemRepository.findById(id)
                .map(orderItem -> {
                    OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
                    orderItemDTO.setPrice(orderItem.getProduct().getPrice()); // Set the price from Product
                    return orderItemDTO;
                });
    }

    @Override
    public List<OrderItemDTO> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(orderItem -> modelMapper.map(orderItem, OrderItemDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public List<OrderItemDTO> getAllOrderItems() {
        return orderItemRepository.findAll().stream()
                .map(orderItem -> {
                    OrderItemDTO orderItemDTO = modelMapper.map(orderItem, OrderItemDTO.class);
                    orderItemDTO.setPrice(orderItem.getProduct().getPrice()); // Set the price from Product
                    return orderItemDTO;
                })
                .collect(Collectors.toList());
    }

    @Override
    public OrderItemDTO updateOrderItem(OrderItemDTO orderItemDTO) {
        Optional<OrderItem> existingOrderItemOptional = orderItemRepository.findById(orderItemDTO.getId());
        if (existingOrderItemOptional.isPresent()) {
            OrderItem existingOrderItem = existingOrderItemOptional.get();

            // Update quantity
            if (orderItemDTO.getQuantity() > 0) {
                existingOrderItem.setQuantity(orderItemDTO.getQuantity());
            }

            // Update product
            if (orderItemDTO.getProduct() != null) {
                Optional<Product> productOptional = productRepository.findById(orderItemDTO.getProduct().getId());
                if (productOptional.isPresent()) {
                    Product product = productOptional.get();
                    existingOrderItem.setProduct(product);
                    existingOrderItem.setPrice(product.getPrice()); // Update the price from Product
                } else {
                    throw new RuntimeException("Product not found for the given ID.");
                }
            }

            OrderItem updatedOrderItem = orderItemRepository.save(existingOrderItem);
            return modelMapper.map(updatedOrderItem, OrderItemDTO.class);
        } else {
            return null;
        }
    }

    @Override
    public void deleteOrderItemById(Long id) {
        orderItemRepository.deleteById(id);
    }
}
