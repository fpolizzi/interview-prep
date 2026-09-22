package com.foodtakeway.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderPlacedTopic(KafkaTopicProperties topicProperties) {
        return TopicBuilder.name(topicProperties.orderPlaced())
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderProcessedTopic(KafkaTopicProperties topicProperties) {
        return TopicBuilder.name(topicProperties.orderProcessed())
                .partitions(1)
                .replicas(1)
                .build();
    }
}
