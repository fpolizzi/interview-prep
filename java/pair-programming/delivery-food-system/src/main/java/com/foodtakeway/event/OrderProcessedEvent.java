package com.foodtakeway.event;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderProcessedEvent {
    private UUID orderId;
    private double finalAmount;
    private String userEmail;
    private Instant processedAt;
}
