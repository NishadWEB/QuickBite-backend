package com.quickbite.backend.A1_controller;

import com.quickbite.backend.A2_service.OrderService;
import com.quickbite.backend.dto.delivery_DTO.DeliveryPartnerNewOrderResponse;
import com.quickbite.backend.dto.order_DTO.*;
import com.quickbite.backend.dto.order_DTO.LiveOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/restaurants/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ CUSTOMER ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

    // fetch all orders of a customer, (order history)
    @GetMapping()
    public ResponseEntity<List<PastOrder>> getOrderHistoryOfCurrentUser(){
        List<PastOrder> res = orderService.getOrderHistoryOfCurrentUser();
        return ResponseEntity.status(200).body(res);
    }

    // fetch live orders of a customer
    @GetMapping("/c/live-orders")
    public ResponseEntity<List<CurrentOrder>> getLiveOrderOfCurrentUser(){
        List<CurrentOrder> res = orderService.getLiveOrderOfCurrentUser();
        return ResponseEntity.status(200).body(res);
    }



//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ RESTAURANT ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

    // to fetch by restaurants in 'New Orders' window (notification)
    @GetMapping("/new-orders")
    public ResponseEntity<List<NewOrdersResponse>> getNewOrdersList() {
        List<NewOrdersResponse> res = orderService.getNewOrdersList();
        return ResponseEntity.status(200).body(res);
    }

    // fetch accepted-orders list (live orders window)
    @GetMapping("/r/live-orders")
    public ResponseEntity<List<LiveOrder>> getLiveOrdersOfThisRestaurant(){
        List<LiveOrder> res = orderService.getLiveOrdersOfThisRestaurant();
        return ResponseEntity.status(200).body(res);
    }

//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ DELIVERY-BOY ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

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

    // to fetch by delivery-partner in 'New Orders' window (notification)
    @GetMapping("/d/new-orders")
    public ResponseEntity<DeliveryPartnerNewOrderResponse> getNewOrdersListOfCurrentDeliveryPartner() {
        DeliveryPartnerNewOrderResponse res = orderService.getNewOrdersListOfCurrentDeliveryPartner();
        return ResponseEntity.status(200).body(res);
    }
}
