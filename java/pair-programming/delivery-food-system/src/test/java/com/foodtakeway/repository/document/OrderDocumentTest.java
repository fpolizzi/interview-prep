package com.foodtakeway.repository.document;

import com.foodtakeway.model.Order;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderDocumentTest {

    @Test
    @DisplayName("Should convert domain Order to OrderDocument")
    void shouldConvertDomainToDocument() {
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .orderId(orderId)
                .amount(150.0)
                .userEmail("user@example.com")
                .isProcessed(true)
                .build();

        OrderDocument document = OrderDocument.fromDomain(order);

        assertThat(document).isNotNull();
        assertThat(document.getOrderId()).isEqualTo(orderId);
        assertThat(document.getAmount()).isEqualTo(150.0);
        assertThat(document.getUserEmail()).isEqualTo("user@example.com");
        assertThat(document.isProcessed()).isTrue();
    }

    @Test
    @DisplayName("Should return null document when domain Order is null")
    void shouldReturnNullWhenDomainIsNull() {
        assertThat(OrderDocument.fromDomain(null)).isNull();
    }

    @Test
    @DisplayName("Should convert OrderDocument to domain Order")
    void shouldConvertDocumentToDomain() {
        UUID orderId = UUID.randomUUID();
        OrderDocument document = OrderDocument.builder()
                .orderId(orderId)
                .amount(250.0)
                .userEmail("customer@test.com")
                .isProcessed(false)
                .build();

        Order order = document.toDomain();

        assertThat(order).isNotNull();
        assertThat(order.getOrderId()).isEqualTo(orderId);
        assertThat(order.getAmount()).isEqualTo(250.0);
        assertThat(order.getUserEmail()).isEqualTo("customer@test.com");
        assertThat(order.isProcessed()).isFalse();
    }
}
