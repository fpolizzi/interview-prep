package com.foodtakeway.repository.document;

import com.foodtakeway.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "orders")
public class OrderDocument {

    @Id
    private UUID orderId;
    private double amount;
    private String userEmail;
    private boolean isProcessed;

    public static OrderDocument fromDomain(Order order) {
        if (order == null) {
            return null;
        }
        return OrderDocument.builder()
                .orderId(order.getOrderId())
                .amount(order.getAmount())
                .userEmail(order.getUserEmail())
                .isProcessed(order.isProcessed())
                .build();
    }

    public Order toDomain() {
        return Order.builder()
                .orderId(this.orderId)
                .amount(this.amount)
                .userEmail(this.userEmail)
                .isProcessed(this.isProcessed)
                .build();
    }
}
