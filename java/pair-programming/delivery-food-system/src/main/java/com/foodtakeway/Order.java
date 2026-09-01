package com.foodtakeway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Document
public class Order {
    @Id
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

    // This method should stay as it is. Just pretend it is a long-time operation we need to do.
    public void longRunningOrderProcess() {
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
