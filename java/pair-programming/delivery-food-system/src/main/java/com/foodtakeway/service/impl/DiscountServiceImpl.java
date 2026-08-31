package com.foodtakeway.service.impl;

import com.foodtakeway.Order;
import com.foodtakeway.service.DiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Created by fpolizzi on 8/25/26
 */
@Service
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final double percentage;

    private final double threshold;

    public DiscountServiceImpl(@Value("${discount.percentage}") double percentage,
                               @Value("${discount.threshold}") double threshold) {
        this.percentage = percentage > 1.0 ? percentage / 100.0 : percentage;
        this.threshold = threshold;
    }

    @Override
    public void calculateDiscount(Order order) {

        log.info("Calculating discount for order {}", order);
        log.info("Discount percentage {}", percentage);
        log.info("Discount threshold {}", threshold);

        if (order.getAmount() > threshold) {
            order.setAmount(order.getAmount() * (1 - percentage)); // 10% discount for orders above 100

            log.info("amount after discount {} ", order.getAmount());
        }
    }
}
