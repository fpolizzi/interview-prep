package com.foodtakeway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document
class Order {
    @Id
    private String orderId;
    private double amount;
    private String userEmail;
    private boolean isProcessed;

    public Order(String orderId, double amount, String userEmail) {
        this.orderId = orderId;
        this.amount = amount;
        this.isProcessed = false;
        this.userEmail = userEmail;
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
