package com.antrika.backend.order.repository;

import com.antrika.backend.order.entity.Order;
import com.antrika.backend.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrder(Order order);
}