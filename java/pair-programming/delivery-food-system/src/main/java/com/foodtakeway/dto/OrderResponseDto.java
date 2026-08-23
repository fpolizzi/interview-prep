package com.foodtakeway.dto;

/**
 * Created by fpolizzi on 8/18/26
 */
import lombok.Builder;

@Builder
public record OrderResponseDto(
        java.util.UUID orderId,
        double amount,
        String userEmail,
        boolean isProcessed
) {
}
