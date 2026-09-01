package com.foodtakeway.service;

import com.foodtakeway.Order;
import com.foodtakeway.OrderRepository;
import com.foodtakeway.dto.OrderResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final DiscountService discountService;


    public OrderService(OrderRepository orderRepository, DiscountService discountService) {
        this.orderRepository = orderRepository;
        this.discountService = discountService;
    }

    // TODO do some research about masking log messages /
    //  logging the right way when sensible data is involved
    // Receive order and persist
    public OrderResponseDto placeOrder(double amount, String userEmail) {

        Order order = new Order(amount, userEmail);
        orderRepository.save(order);

        log.info("Order placed: {} Amount: {} user: {}",
                order.getOrderId(), amount, userEmail);

        processOrder(order);

        notifyEvent("OrderProcessed", String.valueOf(order.getOrderId()));

        return new OrderResponseDto(
                order.getOrderId(),
                order.getAmount(),
                order.getUserEmail(),
                order.isProcessed()
        );
    }

    /**
     * TODO placeOrder() and processOrder() are tightly coupled. What can we do to improve this coupling,
     *  considering that process can be a long running task and be a bottleneck?
     **/
    // Process order (imagine it can be invoicing, notification, stock change, etc.)
    private void processOrder(Order order) {

        discountService.calculateDiscount(order);

        order.longRunningOrderProcess();
        order.setProcessed(true);

        log.info("Order processed: {} Final Amount: {} user: {}",
                order.getOrderId(), order.getAmount(), order.getUserEmail());
    }

    private void notifyEvent(String event, String data) {
        log.info("Event Dispatched: {} -> {}", event, data);
    }
}
