package com.foodtakeway;

import com.foodtakeway.support.TestRepositoryConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@Import(TestRepositoryConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {"order-placed-topic", "order-processed-topic"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class DeliveryFoodSystemApplicationIT {

    @Test
    @DisplayName("Application context loads cleanly with isolated test configuration")
    void contextLoads() {
    }
}
