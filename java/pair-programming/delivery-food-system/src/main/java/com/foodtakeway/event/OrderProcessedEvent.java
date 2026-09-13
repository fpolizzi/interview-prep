package com.foodtakeway.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProcessedEvent {
    private UUID orderId;
    private double finalAmount;
    private String userEmail;
    private Instant processedAt;
}
