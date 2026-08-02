package com.foodtakeway;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("order")
public class OrderApi {

    @Autowired
    OrderService orderService;

    @GetMapping("hello")
    public ResponseEntity<String> hello() {
        return new ResponseEntity<>("Hello World!", HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> placeOrder(@RequestBody OrderRequest orderRequest) {

        log.info("Order received: {}", orderRequest);
        orderService.placeOrder(orderRequest.getAmount(), orderRequest.getUserEmail());

        return new ResponseEntity<>("Order Placed!", HttpStatus.OK);
    }
}

@Data
@NoArgsConstructor
class OrderRequest {
    private String userEmail;
    private double amount;

    public OrderRequest(String userEmail, double amount) {
        this.userEmail = userEmail;
        this.amount = amount;
    }

    public String toString() {
        return "OrderRequest [userEmail=" + userEmail + ", amount=" + amount + "]";
    }
}
