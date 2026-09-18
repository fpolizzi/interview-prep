package com.foodtakeway.service.impl;

import com.foodtakeway.config.KafkaTopicProperties;
import com.foodtakeway.dto.OrderResponseDto;
import com.foodtakeway.event.OrderPlacedEvent;
import com.foodtakeway.model.Order;
import com.foodtakeway.repository.OrderRepository;
import com.foodtakeway.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public OrderServiceImpl(OrderRepository orderRepository,
                            KafkaTemplate<String, Object> kafkaTemplate,
                            KafkaTopicProperties topicProperties) {
        this.orderRepository = orderRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    // Receive order and persist, then emit OrderPlacedEvent for async processing
    @Override
    public OrderResponseDto placeOrder(double amount, String userEmail) {
        Order order = new Order(amount, userEmail);
        orderRepository.save(order);

        log.info("Order placed: {} Amount: {} user: {}",
                order.getOrderId(), amount, userEmail);

        OrderPlacedEvent event = OrderPlacedEvent.builder()
                .orderId(order.getOrderId())
                .amount(order.getAmount())
                .userEmail(order.getUserEmail())
                .createdAt(Instant.now())
                .build();

        kafkaTemplate.send(topicProperties.orderPlaced(), String.valueOf(order.getOrderId()), event);
        log.info("Event Dispatched: OrderPlaced -> {}", order.getOrderId());

        return new OrderResponseDto(
                order.getOrderId(),
                order.getAmount(),
                order.getUserEmail(),
                order.isProcessed()
        );
    }
}
