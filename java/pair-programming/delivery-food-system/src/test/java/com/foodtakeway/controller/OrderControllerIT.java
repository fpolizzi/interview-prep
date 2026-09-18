package com.foodtakeway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtakeway.dto.OrderRequestDto;
import com.foodtakeway.repository.OrderRepository;
import com.foodtakeway.support.TestRepositoryConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@Import(TestRepositoryConfig.class)
@EmbeddedKafka(
        partitions = 1,
        topics = {"order-placed-topic", "order-processed-topic"},
        bootstrapServersProperty = "spring.kafka.bootstrap-servers"
)
class OrderControllerIT {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private OrderRepository orderRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        orderRepository.deleteAll();
    }

    @Test
    @DisplayName("Should submit order through REST API, return 201 immediately, and process via Kafka")
    void shouldCreateOrderAndProcessEndToEnd() throws Exception {
        OrderRequestDto request = OrderRequestDto.builder()
                .amount(250.0)
                .userEmail("vip@example.com")
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").isNotEmpty())
                .andExpect(jsonPath("$.amount").value(250.0))
                .andExpect(jsonPath("$.userEmail").value("vip@example.com"))
                .andExpect(jsonPath("$.isProcessed").value(false));

        // Verify async completion via Kafka listener in in-memory repository
        await()
                .atMost(Duration.ofSeconds(25))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    assertThat(orderRepository.findAll())
                            .hasSize(1)
                            .first()
                            .satisfies(order -> {
                                assertThat(order.isProcessed()).isTrue();
                                assertThat(order.getAmount()).isEqualTo(225.0); // 10% discount on 250
                            });
                });
    }
}
