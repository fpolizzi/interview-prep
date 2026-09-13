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
public class OrderPlacedEvent {
    private UUID orderId;
    private double amount;
    private String userEmail;
    private Instant createdAt;
}
