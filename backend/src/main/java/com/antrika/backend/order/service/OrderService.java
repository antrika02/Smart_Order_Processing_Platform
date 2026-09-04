package com.antrika.backend.order.service;

import com.antrika.backend.entity.User;
import com.antrika.backend.order.dto.CreateOrderItemRequest;
import com.antrika.backend.order.dto.CreateOrderRequest;
import com.antrika.backend.order.dto.OrderItemResponse;
import com.antrika.backend.order.dto.OrderResponse;
import com.antrika.backend.order.entity.Order;
import com.antrika.backend.order.entity.OrderItem;
import com.antrika.backend.order.entity.OrderStatus;
import com.antrika.backend.order.repository.OrderItemRepository;
import com.antrika.backend.order.repository.OrderRepository;
import com.antrika.backend.product.entity.Product;
import com.antrika.backend.product.repository.ProductRepository;
import com.antrika.backend.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            UserRepository userRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        BigDecimal totalAmount = BigDecimal.ZERO;

        Order order = new Order(
                user,
                BigDecimal.ZERO,
                OrderStatus.PENDING
        );

        order = orderRepository.save(order);

        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (CreateOrderItemRequest itemRequest : request.items()) {

            Product product = productRepository
                    .findById(itemRequest.productId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Product not found with id: "
                                            + itemRequest.productId()
                            )
                    );

            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new RuntimeException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            product.setStockQuantity(
                    product.getStockQuantity() - itemRequest.quantity()
            );

            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(
                            BigDecimal.valueOf(itemRequest.quantity())
                    );

            totalAmount = totalAmount.add(itemTotal);

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    itemRequest.quantity(),
                    product.getPrice()
            );

            orderItemRepository.save(orderItem);

            itemResponses.add(
                    new OrderItemResponse(
                            product.getId(),
                            product.getName(),
                            itemRequest.quantity(),
                            product.getPrice()
                    )
            );
        }

        order.setTotalAmount(totalAmount);
        order = orderRepository.save(order);

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}