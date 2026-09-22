package com.foodtakeway.repository;

import com.foodtakeway.model.Order;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Order entity.
 */
public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(UUID orderId);

    List<Order> findAll();

    void deleteById(UUID orderId);

    void deleteAll();

    boolean existsById(UUID orderId);
}
