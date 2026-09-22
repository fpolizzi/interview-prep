package com.foodtakeway.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class KafkaTopicPropertiesTest {

    @Test
    @DisplayName("Should correctly hold topic names")
    void shouldHoldTopicNames() {
        KafkaTopicProperties properties = new KafkaTopicProperties("placed-topic", "processed-topic");

        assertThat(properties.orderPlaced()).isEqualTo("placed-topic");
        assertThat(properties.orderProcessed()).isEqualTo("processed-topic");
    }
}
