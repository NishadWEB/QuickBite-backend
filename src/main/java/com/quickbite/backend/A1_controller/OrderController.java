package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.OrderService;
import com.quickbite.backend.dto.order_DTO.OrderResponseDTO;
import com.quickbite.backend.dto.order_DTO.PendingOrdersResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
}
