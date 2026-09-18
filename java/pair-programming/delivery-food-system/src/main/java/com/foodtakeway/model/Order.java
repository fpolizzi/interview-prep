package com.foodtakeway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

/**
 * Pure domain model representing an Order, decoupled from persistence frameworks.
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    private UUID orderId;
    private double amount;
    private String userEmail;
    private boolean isProcessed;

    public Order(double amount, String userEmail) {
        this.orderId = UUID.randomUUID();
        this.amount = amount;
        this.isProcessed = false;
        this.userEmail = userEmail;
    }

    public void markAsProcessed() {
        this.isProcessed = true;
    }

    // This method should stay as it is. Just pretend it is a long-time operation we need to do.
    public void longRunningOrderProcess() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
