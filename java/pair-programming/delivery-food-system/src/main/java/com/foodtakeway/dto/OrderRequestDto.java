package com.foodtakeway.dto;

/**
 * Created by fpolizzi on 8/23/26
 */
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@Builder
public record OrderRequestDto(
        @NotBlank(message = "user email can not be empty") String userEmail,
        @Positive(message = "amount must be positive") double amount
) {
}
