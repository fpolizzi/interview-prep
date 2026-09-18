package com.foodtakeway.service;

import com.foodtakeway.config.KafkaTopicProperties;
import com.foodtakeway.dto.OrderResponseDto;
import com.foodtakeway.event.OrderPlacedEvent;
import com.foodtakeway.model.Order;
import com.foodtakeway.repository.OrderRepository;
import com.foodtakeway.service.impl.OrderServiceImpl;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Created by fpolizzi on 9/1/26
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Spy
    private KafkaTopicProperties topicProperties = new KafkaTopicProperties("order-placed-topic", "order-processed-topic");

    @InjectMocks
    private OrderServiceImpl underTest;

    @Captor
    private ArgumentCaptor<Order> orderCaptor;

    @Captor
    private ArgumentCaptor<OrderPlacedEvent> eventCaptor;

    private static final double AMOUNT = 100.0;
    private static final String EMAIL = "john.doe@example.com";

    @Test
    @DisplayName("Should persist order as unprocessed")
    void shouldPersistOrderAsUnprocessed() {
        // given
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> {
                    Order o = inv.getArgument(0);
                    return o; // or set an ID if that's what the repo does
                });

        // when
        underTest.placeOrder(AMOUNT, EMAIL);

        // then
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();

        assertAll(
                () -> assertThat(saved.getOrderId()).isNotNull(),
                () -> assertThat(saved.getUserEmail()).isEqualTo(EMAIL),
                () -> assertThat(saved.isProcessed()).isFalse()
        );
    }

    @Test
    @DisplayName("Should return correct response DTO")
    void shouldReturnCorrectResponse() {
        // when
        OrderResponseDto response = underTest.placeOrder(AMOUNT, EMAIL);

        // then
        assertAll(
                () -> assertThat(response.userEmail()).isEqualTo(EMAIL),
                () -> assertThat(response.amount()).isEqualTo(AMOUNT),
                () -> assertThat(response.isProcessed()).isFalse()
        );
    }

    @Test
    @DisplayName("Should publish OrderPlacedEvent to Kafka")
    void shouldPublishOrderPlacedEvent() {
        // given
        when(orderRepository.save(any(Order.class)))
                .thenAnswer(inv -> {
                    Order o = inv.getArgument(0);
                    o.setOrderId(UUID.randomUUID()); // simulate ID assignment
                    return o;
                });

        // when
        underTest.placeOrder(AMOUNT, EMAIL);

        // then
        verify(kafkaTemplate).send(
                eq("order-placed-topic"),
                anyString(),
                eventCaptor.capture()
        );

        OrderPlacedEvent event = eventCaptor.getValue();
        assertAll(
                () -> assertThat(event.getAmount()).isEqualTo(AMOUNT),
                () -> assertThat(event.getUserEmail()).isEqualTo(EMAIL),
                () -> assertThat(event.getCreatedAt()).isNotNull()
        );
    }
}
