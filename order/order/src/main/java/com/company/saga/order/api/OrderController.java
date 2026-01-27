package com.company.saga.order.api;

//package com.example.order.api;

//import com.example.order.service.OrderCommandService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.company.saga.order.api.dto.OrderResponse;
import com.company.saga.order.domain.aggregate.Order;
import com.company.saga.order.service.OrderCommandService;
import com.company.saga.order.service.OrderQueryService;

import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderCommandService commandService;
    private final OrderQueryService queryService;

    public OrderController(OrderCommandService commandService,
                           OrderQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public ResponseEntity<String> createOrder() {
        UUID orderId = commandService.createOrder();
        return ResponseEntity.ok("Order created with id: " + orderId);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable UUID orderId) {
        Order order = queryService.getById(orderId);

        return ResponseEntity.ok(
                new OrderResponse(
                        order.getOrderId(),
                        order.getStatus().name()
                )
        );
    }
}
