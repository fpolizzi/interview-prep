package com.foodtakeway.repository.mongo;

import com.foodtakeway.model.Order;
import com.foodtakeway.repository.OrderRepository;
import com.foodtakeway.repository.document.OrderDocument;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@Profile("!test")
public class MongoOrderRepository implements OrderRepository {

    private final SpringDataMongoOrderRepository mongoRepository;

    public MongoOrderRepository(SpringDataMongoOrderRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
    }

    @Override
    public Order save(Order order) {
        OrderDocument document = OrderDocument.fromDomain(order);
        OrderDocument savedDocument = mongoRepository.save(document);
        return savedDocument.toDomain();
    }

    @Override
    public Optional<Order> findById(UUID orderId) {
        return mongoRepository.findById(orderId)
                .map(OrderDocument::toDomain);
    }

    @Override
    public List<Order> findAll() {
        return mongoRepository.findAll()
                .stream()
                .map(OrderDocument::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID orderId) {
        mongoRepository.deleteById(orderId);
    }

    @Override
    public void deleteAll() {
        mongoRepository.deleteAll();
    }

    @Override
    public boolean existsById(UUID orderId) {
        return mongoRepository.existsById(orderId);
    }
}
