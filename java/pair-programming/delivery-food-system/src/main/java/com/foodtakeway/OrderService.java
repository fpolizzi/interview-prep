package com.foodtakeway;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
class OrderService {
    private List<Order> orders = new ArrayList<>();
    private Random random = new Random();

    // Receive order and persist
    public void placeOrder(double amount, String userEmail) {
        String orderId = "ORD" + random.nextInt(1000);
        Order order = new Order(orderId, amount, userEmail);
        orders.add(order);
        System.out.println("Order placed: " + orderId + " Amount: " + amount + " user: " + userEmail);
        processOrder(order);
        notifyEvent("OrderProcessed", order.getOrderId());
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
        System.out.println("Order processed: " + order.getOrderId() + " Final Amount: " + order.getAmount() + " user: " + order.getUserEmail());
    }


    private void notifyEvent(String event, String data) {
        System.out.println("Event Dispatched: " + event + " -> " + data);
    }
}
