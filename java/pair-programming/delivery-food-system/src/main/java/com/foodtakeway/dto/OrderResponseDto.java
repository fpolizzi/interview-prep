package com.foodtakeway.dto;

/**
 * Created by fpolizzi on 8/18/26
 */
import lombok.Builder;
import java.util.UUID;

@Builder
public record OrderResponseDto(
        UUID orderId,
        double amount,
        String userEmail,
        boolean isProcessed
) {
}
