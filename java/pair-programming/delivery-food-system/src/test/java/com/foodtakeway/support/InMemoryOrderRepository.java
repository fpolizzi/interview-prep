package com.foodtakeway.support;

import com.foodtakeway.model.Order;
import com.foodtakeway.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation of OrderRepository for isolated integration testing
 * without requiring a running MongoDB instance or Testcontainers.
 */
public class InMemoryOrderRepository implements OrderRepository {

    private final Map<UUID, Order> database = new ConcurrentHashMap<>();

    @Override
    public Order save(Order order) {
        if (order.getOrderId() == null) {
            order.setOrderId(UUID.randomUUID());
        }
        database.put(order.getOrderId(), order);
        return order;
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return Optional.ofNullable(database.get(orderId));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(database.values());
    }

    @Override
    public void deleteById(UUID orderId) {
        database.remove(orderId);
    }

    @Override
    public void deleteAll() {
        database.clear();
    }

    @Override
    public boolean existsById(UUID orderId) {
        return database.containsKey(orderId);
    }
}
