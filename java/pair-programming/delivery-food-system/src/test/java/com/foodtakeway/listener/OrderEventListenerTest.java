package com.foodtakeway.listener;

import com.foodtakeway.config.KafkaTopicProperties;
import com.foodtakeway.event.OrderPlacedEvent;
import com.foodtakeway.event.OrderProcessedEvent;
import com.foodtakeway.exception.OrderNotFoundException;
import com.foodtakeway.exception.OrderProcessingException;
import com.foodtakeway.model.Order;
import com.foodtakeway.repository.OrderRepository;
import com.foodtakeway.service.DiscountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderEventListenerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DiscountService discountService;

    @Mock
    private KafkaTemplate<Object, Object> kafkaTemplate;

    @Spy
    private KafkaTopicProperties topicProperties = new KafkaTopicProperties("order-placed-topic", "order-processed-topic");

    @InjectMocks
    private OrderEventListener underTest;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<OrderProcessedEvent> processedEventCaptor;

    @Test
    @DisplayName("Should process order, apply discount, mark as processed and publish OrderProcessedEvent")
    void shouldProcessOrderWhenOrderPlacedEventReceived() {
        // given
        UUID orderId = UUID.randomUUID();
        double initialAmount = 200.0;
        double discountedAmount = 180.0;
        String userEmail = "jane.doe@example.com";

        Order order = new Order(initialAmount, userEmail);
        order.setOrderId(orderId);

        OrderPlacedEvent event = new OrderPlacedEvent(orderId, initialAmount, userEmail, Instant.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        doAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setAmount(discountedAmount);
            return null;
        }).when(discountService).calculateDiscount(any(Order.class));

        // when
        underTest.handleOrderPlaced(event);

        // then
        verify(discountService, times(1)).calculateDiscount(order);
        verify(orderRepository, times(1)).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.isProcessed()).isTrue();
        assertThat(savedOrder.getAmount()).isEqualTo(discountedAmount);

        verify(kafkaTemplate, times(1)).send(
                eq("order-processed-topic"),
                eq(orderId.toString()),
                processedEventCaptor.capture()
        );

        OrderProcessedEvent publishedEvent = processedEventCaptor.getValue();
        assertThat(publishedEvent.getOrderId()).isEqualTo(orderId);
        assertThat(publishedEvent.getFinalAmount()).isEqualTo(discountedAmount);
        assertThat(publishedEvent.getUserEmail()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("Should skip processing if order is already processed (idempotency check)")
    void shouldSkipProcessingWhenOrderAlreadyProcessed() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = new Order(100.0, "jane.doe@example.com");
        order.setOrderId(orderId);
        order.setProcessed(true);

        OrderPlacedEvent event = new OrderPlacedEvent(orderId, 100.0, "jane.doe@example.com", Instant.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // when
        underTest.handleOrderPlaced(event);

        // then
        verify(discountService, never()).calculateDiscount(any());
        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw OrderNotFoundException when order is not found in repository")
    void shouldThrowOrderNotFoundExceptionWhenOrderNotFound() {
        // given
        UUID orderId = UUID.randomUUID();
        OrderPlacedEvent event = new OrderPlacedEvent(orderId, 100.0, "missing@example.com", Instant.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> underTest.handleOrderPlaced(event))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining(orderId.toString());

        verify(discountService, never()).calculateDiscount(any());
        verify(orderRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(any(), any(), any());
    }

    @Test
    @DisplayName("Should throw OrderProcessingException when publishing OrderProcessedEvent fails")
    void shouldThrowExceptionWhenOrderProcessedEventPublishingFails() {
        // given
        UUID orderId = UUID.randomUUID();
        Order order = new Order(100.0, "jane.doe@example.com");
        order.setOrderId(orderId);

        OrderPlacedEvent event = new OrderPlacedEvent(orderId, 100.0, "jane.doe@example.com", Instant.now());
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        CompletableFuture<SendResult<String, Object>> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Kafka broker error"));
        doReturn(failedFuture).when(kafkaTemplate).send(anyString(), anyString(), any());

        // when / then
        assertThatThrownBy(() -> underTest.handleOrderPlaced(event))
                .isInstanceOf(OrderProcessingException.class)
                .hasMessageContaining("Failed to dispatch OrderProcessedEvent");
    }
}
