package com.foodtakeway;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("order")
public class OrderApi {

    private OrderService orderService;

    public OrderApi(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDto> placeOrder(
            @Valid @RequestBody OrderRequest orderRequest) {

        log.info("order received: {}", orderRequest);

        Order createdOrder = orderService.placeOrder(
                orderRequest.getAmount(),
                orderRequest.getUserEmail()
        );

        OrderResponseDto response = new OrderResponseDto(
                createdOrder.getOrderId(),
                createdOrder.getAmount(),
                createdOrder.getUserEmail(),
                createdOrder.isProcessed()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

}

@Data
@NoArgsConstructor
class OrderRequest {
    @NotBlank(message = "Email must not be blank")
    @NotNull(message = "Email must not be null")
    @Email(message = "Email must be a valid email address")
    private String userEmail;
    @Positive(message = "Amount must be positive")
    private double amount;

    public OrderRequest(String userEmail, double amount) {
        this.userEmail = userEmail;
        this.amount = amount;
    }

    public String toString() {
        return "OrderRequest [userEmail=" + userEmail + ", amount=" + amount + "]";
    }
}
