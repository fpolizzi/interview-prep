package com.foodtakeway.service;

import com.foodtakeway.dto.OrderResponseDto;

public interface OrderService {
    OrderResponseDto placeOrder(double amount, String userEmail);
}
