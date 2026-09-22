package com.foodtakeway.repository.mongo;

import com.foodtakeway.model.Order;
import com.foodtakeway.repository.document.OrderDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MongoOrderRepositoryTest {

    @Mock
    private SpringDataMongoOrderRepository mongoRepository;

    @InjectMocks
    private MongoOrderRepository underTest;

    @Test
    @DisplayName("Should save domain Order and return saved Order via MongoRepository")
    void shouldSaveOrder() {
        UUID orderId = UUID.randomUUID();
        Order order = new Order(100.0, "user@test.com");
        order.setOrderId(orderId);

        OrderDocument document = OrderDocument.fromDomain(order);
        when(mongoRepository.save(any(OrderDocument.class))).thenReturn(document);

        Order savedOrder = underTest.save(order);

        assertThat(savedOrder).isNotNull();
        assertThat(savedOrder.getOrderId()).isEqualTo(orderId);
        assertThat(savedOrder.getAmount()).isEqualTo(100.0);
        assertThat(savedOrder.getUserEmail()).isEqualTo("user@test.com");
        verify(mongoRepository).save(any(OrderDocument.class));
    }

    @Test
    @DisplayName("Should find order by ID and map to domain")
    void shouldFindById() {
        UUID orderId = UUID.randomUUID();
        OrderDocument document = OrderDocument.builder()
                .orderId(orderId)
                .amount(120.0)
                .userEmail("test@test.com")
                .isProcessed(false)
                .build();

        when(mongoRepository.findById(orderId)).thenReturn(Optional.of(document));

        Optional<Order> result = underTest.findById(orderId);

        assertThat(result).isPresent();
        assertThat(result.get().getOrderId()).isEqualTo(orderId);
        assertThat(result.get().getAmount()).isEqualTo(120.0);
    }

    @Test
    @DisplayName("Should return empty optional when order not found")
    void shouldReturnEmptyWhenNotFound() {
        UUID orderId = UUID.randomUUID();
        when(mongoRepository.findById(orderId)).thenReturn(Optional.empty());

        Optional<Order> result = underTest.findById(orderId);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find all orders and map to domain list")
    void shouldFindAll() {
        OrderDocument doc1 = OrderDocument.builder().orderId(UUID.randomUUID()).amount(50.0).userEmail("a@a.com").build();
        OrderDocument doc2 = OrderDocument.builder().orderId(UUID.randomUUID()).amount(75.0).userEmail("b@b.com").build();

        when(mongoRepository.findAll()).thenReturn(List.of(doc1, doc2));

        List<Order> orders = underTest.findAll();

        assertThat(orders).hasSize(2);
        assertThat(orders.get(0).getOrderId()).isEqualTo(doc1.getOrderId());
        assertThat(orders.get(1).getOrderId()).isEqualTo(doc2.getOrderId());
    }

    @Test
    @DisplayName("Should delegate deleteById and deleteAll")
    void shouldDelegateDeletions() {
        UUID orderId = UUID.randomUUID();

        underTest.deleteById(orderId);
        verify(mongoRepository).deleteById(orderId);

        underTest.deleteAll();
        verify(mongoRepository).deleteAll();
    }

    @Test
    @DisplayName("Should delegate existsById")
    void shouldDelegateExistsById() {
        UUID orderId = UUID.randomUUID();
        when(mongoRepository.existsById(orderId)).thenReturn(true);

        assertThat(underTest.existsById(orderId)).isTrue();
        verify(mongoRepository).existsById(orderId);
    }
}
