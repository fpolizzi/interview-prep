package com.foodtakeway;

import com.foodtakeway.config.KafkaTopicProperties;
import com.foodtakeway.dto.OrderResponseDto;
import com.foodtakeway.event.OrderPlacedEvent;
import com.foodtakeway.repository.OrderRepository;
import com.foodtakeway.service.OrderService;
import com.foodtakeway.support.TestRepositoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@Import(TestRepositoryConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {"order-placed-topic", "order-processed-topic"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderServiceIT {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Autowired
    private KafkaTopicProperties topicProperties;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Should place order immediately and process it asynchronously via Kafka")
    void testPlaceOrder_EndToEndAsyncProcessing() {
        double originalAmount = 200.0;
        String userEmail = "customer@example.com";

        // Act: Place the order
        long startTime = System.currentTimeMillis();
        OrderResponseDto response = orderService.placeOrder(originalAmount, userEmail);
        long responseTime = System.currentTimeMillis() - startTime;

        // Assert: Immediate response (< 1.5s) and initial state
        assertThat(responseTime)
                .as("Response should return immediately without waiting for long-running process")
                .isLessThan(1500);

        assertThat(response).isNotNull();
        assertThat(response.orderId()).isNotNull();
        assertThat(response.userEmail()).isEqualTo(userEmail);
        assertThat(response.amount()).isEqualTo(originalAmount);
        assertThat(response.isProcessed()).isFalse();

        // Assert: Order is immediately persisted in DB with isProcessed = false
        assertThat(orderRepository.findById(response.orderId()))
                .isPresent()
                .hasValueSatisfying(order -> assertThat(order.isProcessed()).isFalse());

        // Assert: Asynchronous processing completes via Kafka listener
        // Availability polls until the listener calculates discount and sets isProcessed = true
        await()
                .atMost(Duration.ofSeconds(25))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(orderRepository.findById(response.orderId()))
                        .isPresent()
                        .hasValueSatisfying(processedOrder -> {
                            assertThat(processedOrder.isProcessed())
                                    .as("Order should be marked as processed by Kafka consumer")
                                    .isTrue();
                            assertThat(processedOrder.getAmount())
                                    .as("Discount should be applied (10% off)")
                                    .isEqualTo(180.0);
                        }));
    }

    @Test
    @DisplayName("Should handle idempotency if duplicate event is received")
    void testDuplicateOrderProcessingIdempotency() {
        double originalAmount = 200.0;
        String userEmail = "customer@example.com";

        // Place the order initially
        OrderResponseDto response = orderService.placeOrder(originalAmount, userEmail);

        // Wait until the first processing completes (isProcessed = true and amount discounted to 180.0)
        await()
                .atMost(Duration.ofSeconds(25))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(orderRepository.findById(response.orderId()))
                        .isPresent()
                        .hasValueSatisfying(processedOrder -> {
                            assertThat(processedOrder.isProcessed()).isTrue();
                            assertThat(processedOrder.getAmount()).isEqualTo(180.0);
                        }));

        // Send a duplicate OrderPlacedEvent for the same orderId
        OrderPlacedEvent duplicateEvent = new OrderPlacedEvent(
                response.orderId(),
                originalAmount,
                userEmail,
                Instant.now()
        );

        kafkaTemplate.send(
                topicProperties.orderPlaced(),
                response.orderId().toString(),
                duplicateEvent
        );

        // Assert: State remains unchanged (discount is NOT applied a second time)
        await()
                .during(Duration.ofSeconds(3))
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> assertThat(orderRepository.findById(response.orderId()))
                        .isPresent()
                        .hasValueSatisfying(order -> {
                            assertThat(order.isProcessed()).isTrue();
                            assertThat(order.getAmount())
                                    .as("Discount must not be reapplied on duplicate event")
                                    .isEqualTo(180.0);
                        }));
    }
}
