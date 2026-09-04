package com.antrika.backend.order.repository;

import com.antrika.backend.entity.User;
import com.antrika.backend.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
}