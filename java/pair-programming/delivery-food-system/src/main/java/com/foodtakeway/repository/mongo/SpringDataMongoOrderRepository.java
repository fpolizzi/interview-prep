package com.foodtakeway.repository.mongo;

import com.foodtakeway.repository.document.OrderDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SpringDataMongoOrderRepository extends MongoRepository<OrderDocument, UUID> {
}
