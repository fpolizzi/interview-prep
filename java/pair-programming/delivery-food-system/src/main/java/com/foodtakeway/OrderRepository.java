package com.foodtakeway;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by fpolizzi on 8/23/26
 */
@Repository
public interface OrderRepository extends MongoRepository<Order, String> {
}
