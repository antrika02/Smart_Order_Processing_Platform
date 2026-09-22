package com.antrika.backend.order.repository;

import com.antrika.backend.entity.User;
import com.antrika.backend.order.entity.Order;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);

    Optional<Order> findByIdAndUser(Long id, User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM Order o
            WHERE o.id = :id
            AND o.user = :user
            """)
    Optional<Order> findByIdAndUserWithLock(
            @Param("id") Long id,
            @Param("user") User user
    );
}