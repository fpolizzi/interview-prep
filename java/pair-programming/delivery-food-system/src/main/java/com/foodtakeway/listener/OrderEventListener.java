package com.foodtakeway.listener;

import com.foodtakeway.config.KafkaTopicProperties;
import com.foodtakeway.event.OrderPlacedEvent;
import com.foodtakeway.event.OrderProcessedEvent;
import com.foodtakeway.exception.OrderNotFoundException;
import com.foodtakeway.exception.OrderProcessingException;
import com.foodtakeway.model.Order;
import com.foodtakeway.repository.OrderRepository;
import com.foodtakeway.service.DiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class OrderEventListener {

    private final OrderRepository orderRepository;
    private final DiscountService discountService;
    private final KafkaTemplate<Object, Object> kafkaTemplate;
    private final KafkaTopicProperties topicProperties;

    public OrderEventListener(OrderRepository orderRepository,
                              DiscountService discountService,
                              KafkaTemplate<Object, Object> kafkaTemplate,
                              KafkaTopicProperties topicProperties) {
        this.orderRepository = orderRepository;
        this.discountService = discountService;
        this.kafkaTemplate = kafkaTemplate;
        this.topicProperties = topicProperties;
    }

    @KafkaListener(topics = "${app.kafka.topics.order-placed:order-placed-topic}", groupId = "${spring.kafka.consumer.group-id:consumer-group}")
    public void handleOrderPlaced(OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent for order: {}", event.getOrderId());

        Order order = orderRepository.findById(event.getOrderId())
                .orElseThrow(() -> {
                    log.error("Order with id {} not found in repository.", event.getOrderId());
                    return new OrderNotFoundException(event.getOrderId());
                });

        // Idempotency check
        if (order.isProcessed()) {
            log.warn("Order {} has already been processed. Skipping processing.", order.getOrderId());
            return;
        }

        processOrder(order);
    }

    private void processOrder(Order order) {
        discountService.calculateDiscount(order);

        order.longRunningOrderProcess();
        order.setProcessed(true);
        orderRepository.save(order);

        log.info("Order processed: {} Final Amount: {} user: {}",
                order.getOrderId(), order.getAmount(), order.getUserEmail());

        OrderProcessedEvent processedEvent = OrderProcessedEvent.builder()
                .orderId(order.getOrderId())
                .finalAmount(order.getAmount())
                .userEmail(order.getUserEmail())
                .processedAt(Instant.now())
                .build();

        try {
            kafkaTemplate.send(topicProperties.orderProcessed(), String.valueOf(order.getOrderId()), processedEvent)
                    .get(3, TimeUnit.SECONDS);
            log.info("Event Dispatched: OrderProcessed -> {}", order.getOrderId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Thread interrupted while dispatching OrderProcessedEvent for order: {}", order.getOrderId(), e);
            throw new OrderProcessingException("Failed to dispatch OrderProcessedEvent due to thread interruption", e);
        } catch (Exception e) {
            log.error("Failed to dispatch OrderProcessedEvent for order: {}", order.getOrderId(), e);
            throw new OrderProcessingException("Failed to dispatch OrderProcessedEvent", e);
        }
    }
}
