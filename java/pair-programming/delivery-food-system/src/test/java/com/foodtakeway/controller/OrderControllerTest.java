package com.foodtakeway.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.foodtakeway.dto.OrderRequestDto;
import com.foodtakeway.dto.OrderResponseDto;
import com.foodtakeway.exception.GlobalExceptionHandler;
import com.foodtakeway.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrderService orderService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        OrderController orderController = new OrderController(orderService);
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void shouldReturnCreatedWhenRequestIsValid() throws Exception {
        UUID orderId = UUID.randomUUID();
        OrderResponseDto responseDto = new OrderResponseDto(orderId, 50.0, "test@example.com", false);
        when(orderService.placeOrder(anyDouble(), anyString())).thenReturn(responseDto);

        OrderRequestDto requestDto = OrderRequestDto.builder()
                .amount(50.0)
                .userEmail("test@example.com")
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(orderId.toString()))
                .andExpect(jsonPath("$.amount").value(50.0))
                .andExpect(jsonPath("$.userEmail").value("test@example.com"))
                .andExpect(jsonPath("$.isProcessed").value(false));
    }

    @Test
    void shouldReturnBadRequestWithValidationErrorsWhenEmailIsBlank() throws Exception {
        OrderRequestDto requestDto = OrderRequestDto.builder()
                .amount(50.0)
                .userEmail("")
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.userEmail").value("user email can not be empty"));
    }

    @Test
    void shouldReturnBadRequestWithValidationErrorsWhenAmountIsNegative() throws Exception {
        OrderRequestDto requestDto = OrderRequestDto.builder()
                .amount(-10.0)
                .userEmail("test@example.com")
                .build();

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.validationErrors.amount").value("amount must be positive"));
    }
}
