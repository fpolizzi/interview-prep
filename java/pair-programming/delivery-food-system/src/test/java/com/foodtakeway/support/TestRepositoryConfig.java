package com.foodtakeway.support;

import com.foodtakeway.repository.OrderRepository;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class TestRepositoryConfig {

    @Bean
    @Primary
    public OrderRepository orderRepository() {
        return new InMemoryOrderRepository();
    }
}
