package com.foodtakeway.service;

import com.foodtakeway.Order;
import com.foodtakeway.OrderRepository;
import com.foodtakeway.dto.OrderResponseDto;
import com.foodtakeway.event.OrderPlacedEvent;
import com.foodtakeway.listener.OrderEventListener;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
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

    @InjectMocks
    private OrderService underTest;

    @Captor
    private ArgumentCaptor<Order> orderArgumentCaptor;

    @Captor
    private ArgumentCaptor<OrderPlacedEvent> eventArgumentCaptor;

    @Test
    @DisplayName("Should create, persist order as unprocessed, and publish OrderPlacedEvent")
    void shouldPersistOrderAndPublishEventWhenPlaced() {
        // given
        double amount = 100.0;
        String userEmail = "john.doe@example.com";

        // when
        OrderResponseDto response = underTest.placeOrder(amount, userEmail);

        // then
        verify(orderRepository, times(1)).save(orderArgumentCaptor.capture());
        Order savedOrder = orderArgumentCaptor.getValue();

        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getOrderId()).isNotNull();
        assertThat(savedOrder.getUserEmail()).isEqualTo(userEmail);
        assertThat(savedOrder.isProcessed()).isFalse();

        assertThat(response.userEmail()).isEqualTo(userEmail);
        assertThat(response.amount()).isEqualTo(amount);
        assertThat(response.isProcessed()).isFalse();

        verify(kafkaTemplate, times(1)).send(
                eq(OrderEventListener.TOPIC_ORDER_PLACED),
                eq(savedOrder.getOrderId().toString()),
                eventArgumentCaptor.capture()
        );

        OrderPlacedEvent publishedEvent = eventArgumentCaptor.getValue();
        assertThat(publishedEvent.getOrderId()).isEqualTo(savedOrder.getOrderId());
        assertThat(publishedEvent.getAmount()).isEqualTo(amount);
        assertThat(publishedEvent.getUserEmail()).isEqualTo(userEmail);
        assertThat(publishedEvent.getCreatedAt()).isNotNull();
    }
}
