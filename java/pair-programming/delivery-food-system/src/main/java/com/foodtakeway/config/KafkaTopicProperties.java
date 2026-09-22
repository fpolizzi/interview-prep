package com.foodtakeway.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.kafka.topics")
public record KafkaTopicProperties(
        @NotBlank(message = "Order placed topic name must not be blank")
        String orderPlaced,

        @NotBlank(message = "Order processed topic name must not be blank")
        String orderProcessed
) {
}
