package com.foodtakeway.service.impl;

import com.foodtakeway.config.KafkaTopicProperties;
import com.foodtakeway.dto.OrderResponseDto;
import com.foodtakeway.event.OrderPlacedEvent;
import com.foodtakeway.exception.OrderProcessingException;
import com.foodtakeway.model.Order;
import com.foodtakeway.repository.OrderRepository;
import com.foodtakeway.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public OrderServiceImpl(OrderRepository orderRepository,
                            KafkaTemplate<Object, Object> kafkaTemplate,
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

        try {
            kafkaTemplate.send(topicProperties.orderPlaced(), String.valueOf(order.getOrderId()), event)
                    .get(3, TimeUnit.SECONDS);
            log.info("Event Dispatched: OrderPlaced -> {}", order.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while publishing OrderPlacedEvent for order: {}", order.getOrderId(), e);
            orderRepository.deleteById(order.getOrderId());
            throw new OrderProcessingException("Order could not be queued for processing due to thread interruption", e);
        } catch (Exception e) {
            log.error("Failed to publish OrderPlacedEvent for order: {}", order.getOrderId(), e);
            orderRepository.deleteById(order.getOrderId());
            throw new OrderProcessingException("Order could not be queued for processing", e);
        }

        return new OrderResponseDto(
                order.getOrderId(),
                order.getAmount(),
                order.getUserEmail(),
                order.isProcessed()
        );
    }
}
