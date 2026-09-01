package com.foodtakeway.service;

import com.foodtakeway.Order;
import com.foodtakeway.OrderRepository;
import com.foodtakeway.dto.OrderResponseDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by fpolizzi on 9/1/26
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private DiscountService discountService;

    @InjectMocks
    private OrderService underTest;

    @Captor
    private ArgumentCaptor<Order> orderArgumentCaptor;

    @Test
    @DisplayName("Should create and persist order with correct user details and initial amount")
    void shouldPersistOrderWhenPlaced() {
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
        assertThat(response.userEmail()).isEqualTo(userEmail);
    }

    @Test
    @DisplayName("Should apply discount when DiscountService reduces the order amount")
    void shouldApplyDiscountWhenEligible() {
        // given
        double initialAmount = 200.0;
        double discountedAmount = 180.0;
        String userEmail = "jane.doe@example.com";

        // Mock DiscountService modifying the order's amount
        doAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            order.setAmount(discountedAmount);
            return null;
        }).when(discountService).calculateDiscount(any(Order.class));

        // when
        OrderResponseDto response = underTest.placeOrder(initialAmount, userEmail);

        // then
        verify(discountService).calculateDiscount(any(Order.class));
        assertThat(response.amount()).isEqualTo(discountedAmount);
        assertThat(response.isProcessed()).isTrue();
    }

    @Test
    @DisplayName("Should not apply discount when DiscountService does not modify amount")
    void shouldNotApplyDiscountWhenNotEligible() {
        // given
        double amount = 50.0;
        String userEmail = "jane.doe@example.com";

        // DiscountService does not change number of orders under threshold
        doNothing().when(discountService).calculateDiscount(any(Order.class));

        // when
        OrderResponseDto response = underTest.placeOrder(amount, userEmail);

        // then
        verify(discountService).calculateDiscount(any(Order.class));
        assertThat(response.amount()).isEqualTo(amount);
        assertThat(response.isProcessed()).isTrue();
    }
}
