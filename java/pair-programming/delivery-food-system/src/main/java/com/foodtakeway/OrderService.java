package com.foodtakeway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
class OrderService {
    private final List<Order> orders = new ArrayList<>();
    private final Random random = new Random();

    // Receive order and persist
    public Order placeOrder(double amount, String userEmail) {
        String orderId = "ORD" + random.nextInt(1000);
        Order order = new Order(orderId, amount, userEmail);
        orders.add(order);
        log.info("Order placed: {} Amount: {} user: {}",
                orderId, amount, userEmail);
        processOrder(order);
        notifyEvent("OrderProcessed", order.getOrderId());
        return order;
    }

    /**
     * TODO placeOrder() and processOrder() are tightly coupled. What can we do to improve this coupling,
     *  considering that process can be a long running task and be a bottleneck?
     **/
    // Process order (imagine it can be invoicing, notification, stock change, etc.)
    private void processOrder(Order order) {
        if (order.getAmount() > 100) {
            order.setAmount(order.getAmount() * 0.9); // 10% discount for orders above 100
        }
        order.longRunningOrderProcess();
        order.setProcessed(true);
        log.info("Order processed: {} Final Amount: {} user: {}",
                order.getOrderId(), order.getAmount(), order.getUserEmail());
    }


    private void notifyEvent(String event, String data) {
        log.info("Event Dispatched: {} -> {}", event, data);
    }
}
