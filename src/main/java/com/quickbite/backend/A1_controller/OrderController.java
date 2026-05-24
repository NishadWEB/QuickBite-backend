package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.OrderService;
import com.quickbite.backend.dto.order_DTO.CurrentOrderResponse;
import com.quickbite.backend.dto.order_DTO.LiveOrderResponse;
import com.quickbite.backend.dto.order_DTO.PendingOrdersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping()
    public ResponseEntity<String> placeOrder(){
        String res = orderService.placeOrder();
        return ResponseEntity.status(200).body(res);
    }

//     to fetch by restaurants in 'New Orders' window
    @GetMapping("/pending")
    public ResponseEntity<List<PendingOrdersResponse>> getPendingOrders(){
        List<PendingOrdersResponse> res = orderService.getPendingOrders();
        return ResponseEntity.status(200).body(res);
    }

    // Live order tracking by 'Customer'
    @GetMapping("/current-orders")
    public ResponseEntity<CurrentOrderResponse> getCurrentOrder(){
        CurrentOrderResponse res = orderService.getCurrentOrder();
        return ResponseEntity.status(200).body(res);
    }
}
