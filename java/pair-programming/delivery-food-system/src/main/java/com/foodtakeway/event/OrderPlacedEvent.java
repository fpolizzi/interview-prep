package com.foodtakeway.event;

import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderPlacedEvent {
    private UUID orderId;
    private double amount;
    private String userEmail;
    private Instant createdAt;
}
