package com.foodtakeway.config;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "discount")
public record DiscountProperties(
        @DecimalMin(value = "0.0", message = "Percentage cannot be negative")
        double percentage,

        @PositiveOrZero(message = "Threshold must be zero or positive")
        double threshold
) {
    public double normalizedPercentage() {
        return percentage > 1.0 ? percentage / 100.0 : percentage;
    }
}
