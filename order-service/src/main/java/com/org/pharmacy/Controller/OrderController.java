package com.org.pharmacy.Controller;

import com.org.pharmacy.Enum.OrderStatus;
import com.org.pharmacy.Events.OrderItemDTO;
import com.org.pharmacy.Service.OrderService;
import com.org.pharmacy.Entity.Order;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController @RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest req) {
        log.info("Received Order Request");
        Order order = orderService.placeOrder(req.getUserId(), req.getItems());
        return ResponseEntity.ok(new CreateOrderResponse(order.getId(), order.getStatus(), order.getTotalAmount()));
    }

    @Data static class CreateOrderRequest {
        private Long userId;
        private List<OrderItemDTO> items;
    }

    @Data static class CreateOrderResponse {
        private Long orderId; private OrderStatus status; private Object totalAmount;
        public CreateOrderResponse(Long id, OrderStatus s, Object t){ this.orderId = id; this.status=s; this.totalAmount = t; }
    }
}

