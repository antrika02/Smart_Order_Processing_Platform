package com.antrika.backend.order.repository;

import com.antrika.backend.entity.User;
import com.antrika.backend.order.entity.IdempotencyKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdempotencyKeyRepository
        extends JpaRepository<IdempotencyKey, Long> {

    Optional<IdempotencyKey> findByUserAndIdempotencyKey(
            User user,
            String idempotencyKey
    );
}