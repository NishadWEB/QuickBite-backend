package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.*;
import com.quickbite.backend.dto.order_DTO.*;
import com.quickbite.backend.enums.OrderStatus;

import com.quickbite.backend.custom_exception.ResourceNotFoundException;
import com.quickbite.backend.model.*;
import com.quickbite.backend.principal.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Transactional
public class OrderService {

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private OrderItemRepo orderItemRepo;

    @Autowired
    private RestaurantRepo restaurantRepo;

    @Autowired
    private CartRepo cartRepo;

    @Autowired
    private UserRepo userRepo;

    public String placeOrder() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        // fetch the all cart-items of current user
        List<Cart> rawCartItems = cartRepo.findByUserUserId(userId);

        if (rawCartItems.isEmpty()) {
            throw new ResourceNotFoundException("Your cart is empty");
        }

        Double netTotal = 0.0;

        for (Cart item : rawCartItems) {
            netTotal += item.getTotal();
        }

        Restaurant restaurant = rawCartItems.stream().findFirst().get().getRestaurant();

        // set the order summary
        Order order = new Order();
        order.setRestaurant(restaurant);
        order.setUserId(userId);
        order.setTotal(netTotal);
        order.setStatus(OrderStatus.PENDING);

        // save the order summary
        try {
            orderRepo.save(order);
            System.out.println("item is saved to orders table successfully.");
        } catch (Exception e) {
            log.error("error in OrderService ,placeOrder() while saving to orderRepo : " + e);
            throw new RuntimeException(e);
        }


        // below code is to set each cart-items in 'order_items' table by mapping
        for (Cart item : rawCartItems) {

            OrderItem orderItem = new OrderItem();

            orderItem.setOrder(order);
            orderItem.setUser(item.getUser());
            orderItem.setUserName(item.getUser().getName());

            orderItem.setRestaurant(item.getRestaurant());
            orderItem.setRestaurantName(item.getRestaurant().getName());

            orderItem.setDish(item.getDish());
            orderItem.setDishName(item.getDishName());

            orderItem.setPrice(item.getDishPrice());
            orderItem.setQty(item.getQty());
            orderItem.setTotal(item.getTotal());

            try {
                orderItemRepo.save(orderItem);
                cartRepo.deleteAllByUserUserId(userId);
                System.out.println("item(s) saved to orderItems table successfully.");
            } catch (Exception e) {
                log.error("Error in OrderService placeOrder() while saving orderItem : " + e);
                throw new RuntimeException(e);
            }
        }

        return "your request is processed, check the order status";
    }


    public List<PendingOrdersResponse> getPendingOrders() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();
        Integer restaurantId = restaurantRepo.findByUserUserId(userId).getRestaurantId();

        // this is error bcause , status is stored in the orders table ,. not every item has status,.. but every order has a status and inside the order there are multiple items
        List<Order> pendingOrders = orderRepo.findByRestaurantRestaurantIdAndStatus(restaurantId, OrderStatus.PENDING);

        if (pendingOrders.isEmpty()) {
            throw new ResourceNotFoundException("You have no live orders...");
        }

        ArrayList<PendingOrdersResponse> listOfNewOrders = new ArrayList<>();
        for (Order order : pendingOrders) {
            Integer orderId = order.getOrderId();

            ArrayList<OrderItem> rawItems = orderItemRepo.findByOrderOrderId(orderId);

            // just basic mapping for intended response
            List<DishResponse> items = rawItems.stream()
                    .map((i) -> {
                        DishResponse dish = new DishResponse();
                        dish.setDishName(i.getDishName());
                        dish.setQty(i.getQty());
                        dish.setUserId(i.getUser().getUserId());
                        dish.setUserName(i.getUser().getName());
                        dish.setAddress(order.getAddress());

                        return dish;
                    })
                    .toList();

            PendingOrdersResponse newOrder = new PendingOrdersResponse();
            newOrder.setOrderId(orderId);
            newOrder.setStatus(order.getStatus());
            newOrder.setItems(items);

            listOfNewOrders.add(newOrder);
        }

        return listOfNewOrders;
    }

    public CurrentOrderResponse getCurrentOrder() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        List<Order> orders = orderRepo.findByUserId(userId);

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("You don't have any live orders.");
        }

        List<LiveOrderResponse> listOfOrders = new ArrayList<>();
        for (Order order : orders) {

            Integer orderId = order.getOrderId();
            List<OrderItem> rawOrderItems = orderItemRepo.findByOrderOrderId(orderId);

            List<Item> orderItems = rawOrderItems.stream().map((i) -> {
                Item item = new Item();
                item.setDishId(i.getDish().getDishId());
                item.setDishName(i.getDish().getName());
                item.setQty(i.getQty());
                item.setPrice(i.getPrice());
                item.setTotal(i.getTotal());

                return item;
            }).toList();

            LiveOrderResponse response = new LiveOrderResponse();
            response.setOrderId(orderId);
            response.setStatus(order.getStatus());
            response.setRestaurantId(order.getRestaurant().getRestaurantId());
            response.setRestaurantName(order.getRestaurant().getName());
            response.setItems(orderItems);
            response.setTotal(order.getTotal());

            listOfOrders.add(response);
        }

        Double netTotal = 0.0;
        for (LiveOrderResponse order : listOfOrders){
            netTotal += order.getTotal();
        }

        CurrentOrderResponse res = new CurrentOrderResponse();
        res.setOrders(listOfOrders);
        res.setNetTotal(netTotal);

        return res;
    }
}
