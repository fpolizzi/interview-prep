package com.foodtakeway.service.impl;

import com.foodtakeway.model.Order;
import com.foodtakeway.config.DiscountProperties;
import com.foodtakeway.service.DiscountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by fpolizzi on 8/25/26
 */
@Service
@Slf4j
public class DiscountServiceImpl implements DiscountService {

    private final DiscountProperties discountProperties;

    @Autowired
    public DiscountServiceImpl(DiscountProperties discountProperties) {
        this.discountProperties = discountProperties;
    }

    public DiscountServiceImpl(double percentage, double threshold) {
        this(new DiscountProperties(percentage, threshold));
    }

    @Override
    public void calculateDiscount(Order order) {
        double percentage = discountProperties.normalizedPercentage();
        double threshold = discountProperties.threshold();

        log.info("Calculating discount for order {}", order);
        log.info("Discount percentage {}", percentage);
        log.info("Discount threshold {}", threshold);

        if (order.getAmount() > threshold) {
            order.setAmount(order.getAmount() * (1 - percentage)); // discount for orders above threshold

            log.info("amount after discount {} ", order.getAmount());
        }
    }
}
