package com.antrika.backend.order.dto;

import com.antrika.backend.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
        Long id,
        BigDecimal totalAmount,
        OrderStatus status,
        LocalDateTime createdAt,
        List<OrderItemResponse> items
) {}