package com.foodtakeway.service;

/**
 * Created by fpolizzi on 8/25/26
 */
import com.foodtakeway.Order;

public interface DiscountService {

    void calculateDiscount(Order order);
}
