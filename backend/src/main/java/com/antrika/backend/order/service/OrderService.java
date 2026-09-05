package com.antrika.backend.order.service;

import com.antrika.backend.entity.User;
import com.antrika.backend.order.dto.CreateOrderItemRequest;
import com.antrika.backend.order.dto.CreateOrderRequest;
import com.antrika.backend.order.dto.OrderItemResponse;
import com.antrika.backend.order.dto.OrderResponse;
import com.antrika.backend.order.entity.Order;
import com.antrika.backend.order.entity.OrderItem;
import com.antrika.backend.order.entity.OrderStatus;
import com.antrika.backend.order.exception.InsufficientStockException;
import com.antrika.backend.order.exception.OrderNotFoundException;
import com.antrika.backend.order.repository.OrderItemRepository;
import com.antrika.backend.order.repository.OrderRepository;
import com.antrika.backend.product.entity.Product;
import com.antrika.backend.product.exception.ProductNotFoundException;
import com.antrika.backend.product.repository.ProductRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.antrika.backend.order.exception.OrderCancellationException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;

    public OrderService(
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

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
                            new ProductNotFoundException(
                                    "Product not found with id: "
                                            + itemRequest.productId()
                            )
                    );

            if (product.getStockQuantity() < itemRequest.quantity()) {
                throw new InsufficientStockException(
                        "Insufficient stock for product: "
                                + product.getName()
                );
            }

            product.setStockQuantity(
                    product.getStockQuantity()
                            - itemRequest.quantity()
            );

            productRepository.save(product);

            BigDecimal itemTotal = product.getPrice()
                    .multiply(
                            BigDecimal.valueOf(
                                    itemRequest.quantity()
                            )
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

    public List<OrderResponse> getMyOrders() {

        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        List<Order> orders = orderRepository.findByUser(user);

        return orders.stream()
                .map(order -> {

                    List<OrderItemResponse> items =
                            orderItemRepository.findByOrder(order)
                                    .stream()
                                    .map(item -> new OrderItemResponse(
                                            item.getProduct().getId(),
                                            item.getProduct().getName(),
                                            item.getQuantity(),
                                            item.getPrice()
                                    ))
                                    .toList();

                    return new OrderResponse(
                            order.getId(),
                            order.getTotalAmount(),
                            order.getStatus(),
                            order.getCreatedAt(),
                            items
                    );
                })
                .toList();
    }

    public OrderResponse getOrderById(Long orderId) {

        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Order order = orderRepository
                .findByIdAndUser(orderId, user)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

        List<OrderItemResponse> items =
                orderItemRepository.findByOrder(order)
                        .stream()
                        .map(item -> new OrderItemResponse(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                items
        );
    }

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {

        User user = (User) SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getPrincipal();

        Order order = orderRepository
                .findByIdAndUser(orderId, user)
                .orElseThrow(() ->
                        new OrderNotFoundException(
                                "Order not found with id: " + orderId
                        )
                );

       if (order.getStatus() != OrderStatus.PENDING) {
           throw new OrderCancellationException(
            "Order cannot be cancelled in its current status"
            );
        }

        List<OrderItem> items =
                orderItemRepository.findByOrder(order);

        for (OrderItem item : items) {

            Product product = item.getProduct();

            product.setStockQuantity(
                    product.getStockQuantity()
                            + item.getQuantity()
            );

            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);

        order = orderRepository.save(order);

        List<OrderItemResponse> itemResponses =
                items.stream()
                        .map(item -> new OrderItemResponse(
                                item.getProduct().getId(),
                                item.getProduct().getName(),
                                item.getQuantity(),
                                item.getPrice()
                        ))
                        .toList();

        return new OrderResponse(
                order.getId(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt(),
                itemResponses
        );
    }
}