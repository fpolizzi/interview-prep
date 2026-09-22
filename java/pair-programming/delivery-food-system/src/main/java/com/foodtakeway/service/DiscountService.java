package com.foodtakeway.service;

/*
 * Created by fpolizzi on 8/25/26
 */
import com.foodtakeway.model.Order;

public interface DiscountService {

    void calculateDiscount(Order order);
}
