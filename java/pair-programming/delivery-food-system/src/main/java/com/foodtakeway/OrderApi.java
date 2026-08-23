package com.foodtakeway;

import com.foodtakeway.dto.OrderRequestDto;
import jakarta.validation.Valid;
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
    public ResponseEntity<Order> placeOrder(@Valid @RequestBody OrderRequestDto orderRequest) {
        log.info("order received {}", orderRequest);

        var orderDTO = orderService.placeOrder(orderRequest.amount(), orderRequest.userEmail());


        return ResponseEntity.status(HttpStatus.CREATED).body(orderDTO);
    }
}
