package com.foodtakeway.service.impl;

import com.foodtakeway.Order;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by fpolizzi on 8/25/26
 */
@Slf4j
class DiscountServiceImplTest {

    private DiscountServiceImpl underTest;

    @BeforeEach
    void setUp() {
        double percentage = 0.10; // 10% discount (different from application.yaml)
        double threshold = 100.00; // 100.00 threshold

        underTest = new DiscountServiceImpl(percentage, threshold);
    }

    @Test
    void itShouldApplyDiscountWhenOrderSumIsGreaterThanThreshold() {
        // given
        Order order = new Order(200.00, "curt@gmail.com");

        // when
        underTest.calculateDiscount(order);

        // then - 200.00 with 10% discount = 180.00
        assertThat(order.getAmount()).isEqualTo(180.00);
    }

    @Test
    void itShouldNotApplyDiscountWhenOrderSumIsEqualToThreshold() {
        // given
        Order order = new Order(100.00, "curt@gmail.com");

        // when
        underTest.calculateDiscount(order);

        // then
        assertThat(order.getAmount()).isEqualTo(100.00);
    }

    @Test
    void itShouldNotApplyDiscountWhenOrderSumIsLessThanThreshold() {
        // given
        Order order = new Order(50.00, "curt@gmail.com");

        // when
        underTest.calculateDiscount(order);

        // then
        assertThat(order.getAmount()).isEqualTo(50.00);
    }

    @Test
    void itShouldSupportWholeNumberPercentageInput() {
        // given percentage configured as 20 (meaning 20%) instead of 0.20
        DiscountServiceImpl serviceWithWholePercentage = new DiscountServiceImpl(20.0, 100.00);
        Order order = new Order(200.00, "curt@gmail.com");

        // when
        serviceWithWholePercentage.calculateDiscount(order);

        // then - 200.00 with 20% discount = 160.00
        assertThat(order.getAmount()).isEqualTo(160.00);
    }

    @Test
    void itShouldSupportDecimalFractionPercentageInput() {
        // given percentage configured as 0.20 (meaning 20%)
        DiscountServiceImpl serviceWithDecimalPercentage = new DiscountServiceImpl(0.20, 100.00);
        Order order = new Order(200.00, "curt@gmail.com");

        // when
        serviceWithDecimalPercentage.calculateDiscount(order);

        // then - 200.00 with 20% discount = 160.00
        assertThat(order.getAmount()).isEqualTo(160.00);
    }
}