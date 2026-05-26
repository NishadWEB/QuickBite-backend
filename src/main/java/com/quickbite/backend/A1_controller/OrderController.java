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
    public ResponseEntity<String> placeOrder() {
        String res = orderService.placeOrder();
        return ResponseEntity.status(200).body(res);
    }

    // CANCEL order by customer
    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<String> cancelOrder(@PathVariable Integer orderId) {
        String res = orderService.cancelOrder(orderId);
        return ResponseEntity.status(200).body(res);
    }

    // ACCEPT order by restaurant
    @PatchMapping("/{orderId}/accept")
    public ResponseEntity<String> acceptOrder(@PathVariable Integer orderId) {
        String res = orderService.acceptOrder(orderId);
        return ResponseEntity.status(200).body(res);
    }

    // REJECT order by restaurant
    @PatchMapping("/{orderId}/reject")
    public ResponseEntity<String> rejectOrder(@PathVariable Integer orderId) {
        String res = orderService.rejectOrder(orderId);
        return ResponseEntity.status(200).body(res);
    }

    // READY order by restaurant
    @PatchMapping("/{orderId}/ready")
    public ResponseEntity<String> markOrderAsReady(@PathVariable Integer orderId) {
        String res = orderService.markOrderAsReady(orderId);
        return ResponseEntity.status(200).body(res);
    }

    // order picked by delivery-boy
    @PatchMapping("/{orderId}/pickup")
    public ResponseEntity<String> markOrderAsOutForDelivery(@PathVariable Integer orderId) {
        String res = orderService.markOrderAsOutForDelivery(orderId);
        return ResponseEntity.status(200).body(res);
    }

    // order ARRIVED by delivery-boy
    @PatchMapping("/{orderId}/arrive")
    public ResponseEntity<String> markOrderAsArrived(@PathVariable Integer orderId) {
        String res = orderService.markOrderAsArrived(orderId);
        return ResponseEntity.status(200).body(res);
    }

    // order DELIVERED  by delivery-boy
    @PatchMapping("/{orderId}/deliver")
    public ResponseEntity<String> markOrderAsDelivered(@PathVariable Integer orderId) {
        String res = orderService.markOrderAsDelivered(orderId);
        return ResponseEntity.status(200).body(res);
    }

    // Live order tracking by 'Customer'
    @GetMapping("/current-orders")
    public ResponseEntity<CurrentOrderResponse> getCurrentOrders() {
        CurrentOrderResponse res = orderService.getCurrentOrders();
        return ResponseEntity.status(200).body(res);
    }

    // to fetch by restaurants in 'New Orders' window
    @GetMapping("/pending")
    public ResponseEntity<List<PendingOrdersResponse>> getPendingOrders() {
        List<PendingOrdersResponse> res = orderService.getPendingOrders();
        return ResponseEntity.status(200).body(res);
    }

}
