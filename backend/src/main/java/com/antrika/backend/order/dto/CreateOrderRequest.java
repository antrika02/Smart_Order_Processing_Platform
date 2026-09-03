package com.antrika.backend.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(

        @NotEmpty(message = "Order must contain at least one item")
        List<@Valid CreateOrderItemRequest> items

) {}