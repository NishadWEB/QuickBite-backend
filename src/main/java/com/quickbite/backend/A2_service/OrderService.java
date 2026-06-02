package com.quickbite.backend.A2_service;

import com.quickbite.backend.A3_repo.*;
import com.quickbite.backend.dto.delivery_DTO.DeliveryPartnerNewOrderResponse;
import com.quickbite.backend.dto.order_DTO.*;
import com.quickbite.backend.dto.order_DTO.LiveOrder;
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
import java.util.Optional;

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

    @Autowired
    private AssignmentStateRepo assignmentStateRepo;

    @Autowired
    private DeliveryPartnerRepo deliveryPartnerRepo;

    public String placeOrder() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        // fetch the all cart-items of current user
        List<Cart> rawCartItems = cartRepo.findByUserUserId(userId);

        if (rawCartItems.isEmpty()) {
            throw new ResourceNotFoundException("Your cart is empty");
        }
        Restaurant restaurant = rawCartItems.stream().findFirst().get().getRestaurant();
        if (restaurant == null || !restaurant.getActive()) {
            throw new ResourceNotFoundException("the restaurant from which you are trying to order is DEACTIVATED after you added the dish to cart.Please clear the cart and try to order from different restaurant.Thankyou for the cooperation");
        }

        Double netTotal = 0.0;

        for (Cart item : rawCartItems) {
            netTotal += item.getTotal();
        }


        // set the order summary
        Order order = new Order();
        order.setRestaurant(restaurant);
        order.setUserId(userId);
        order.setTotal(netTotal);
        order.setStatus(OrderStatus.PLACED);

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
                System.out.println("item(s) saved to orderItems table successfully.");
            } catch (Exception e) {
                log.error("Error in OrderService placeOrder() while saving orderItem : " + e);
                throw new RuntimeException(e);
            }
        }
        cartRepo.deleteAllByUserUserId(userId);

        return "your request is processed, check the order status";
    }

    public String acceptOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("there is no new order with 'order-id = " + orderId + "'"));

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Only placed orders can be accepted");
        }

        order.setStatus(OrderStatus.ACCEPTED);
        try {
            orderRepo.save(order);
            return "Order is accepted";
        } catch (Exception e) {
            log.error("error in OrderService, acceptOrder() while orderRepo.save(order) : " + e);
            throw new RuntimeException(e);
        }
    }

    public String rejectOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("there is no new order with 'order-id = " + orderId + "'"));

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new IllegalStateException("Only placed orders can be rejected");
        }

        order.setStatus(OrderStatus.REJECTED);
        try {
            orderRepo.save(order);
            return "Order is Rejected by restaurant for some reason.Sorry for the inconvenience";
        } catch (Exception e) {
            log.error("error in OrderService, rejectOrder() while orderRepo.save(order) : " + e);
            throw new RuntimeException(e);
        }
    }

    public String cancelOrder(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("There is no order with order-id = " + orderId));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalStateException("This order is already cancelled.");
        } else if (order.getStatus() == OrderStatus.REJECTED) {
            throw new IllegalStateException("This order was already rejected by the restaurant.");
        } else if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new IllegalStateException("Delivered orders cannot be cancelled.");
        } else if (order.getStatus() == OrderStatus.ACCEPTED || order.getStatus() == OrderStatus.READY || order.getStatus() == OrderStatus.OUT_FOR_DELIVERY || order.getStatus() == OrderStatus.ARRIVED) {
            throw new IllegalStateException("This order is already accepted and being prepared by the restaurant. Cancelling this order now may result in food wastage, and a cancellation fee may be charged.");
        }


        // Customer can cancel when staus is Placed
        order.setStatus(OrderStatus.CANCELLED);
        try {
            orderRepo.save(order);
            return "Order cancelled successfully.";
        } catch (Exception e) {
            log.error("Error in OrderService, cancelOrder() : " + e);
            throw new RuntimeException(e);
        }
    }

    public String markOrderAsReady(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("there is no new order with 'order-id = " + orderId + "'"));

        if (order.getStatus() != OrderStatus.ACCEPTED) {
            throw new IllegalStateException("Only ACCEPTED orders can be marked as READY");
        }

        order.setStatus(OrderStatus.READY);
        try {
            orderRepo.save(order);
            Integer deliveryPartner = chooseDeliveryPartner();
            assignOrderToDeliveryPartner(order, deliveryPartner); // userId
            return "Order is Ready.Waiting to pickup by the delivery-partner";
        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("error in OrderService, orderReady() : " + e);
            throw new RuntimeException(e);
        }
    }

    public Integer chooseDeliveryPartner() {
        Integer lastAssignedPartnerId = 0;

        AssignmentState assignedPartner = assignmentStateRepo.findById(1).orElse(null);

        if (assignedPartner != null) {
            lastAssignedPartnerId = assignedPartner.getLastAssignedPartnerId();
        }

        Integer nextDeliveryPartnerId = lastAssignedPartnerId + 1;
        DeliveryPartner deliveryPartner = deliveryPartnerRepo.findById(nextDeliveryPartnerId).orElse(null);

        if (deliveryPartner == null) {
            deliveryPartner = deliveryPartnerRepo.findById(1).orElseThrow(() -> new ResourceNotFoundException("there is no single delivery partners registered to the system yet! please be patient."));
        }

        // updating the lastAssignedPartnerId for next time
        if (assignedPartner != null) {
            assignedPartner.setLastAssignedPartnerId(deliveryPartner.getDeliveryPartnerId());
        } else {
            AssignmentState newPartner = new AssignmentState();
            newPartner.setLastAssignedPartnerId(deliveryPartner.getDeliveryPartnerId());
            try {
                assignmentStateRepo.save(newPartner);
            } catch (Exception e) {
                log.error("error in OrderService chooseDeliveryParnter() : " + e);
                throw new RuntimeException(e);
            }
        }
        return deliveryPartner.getDeliveryPartnerId();
    }

    public void assignOrderToDeliveryPartner(Order order, Integer deliveryPartnerId) {
        // assigning part is here
        order.setDeliveryPartnerId(deliveryPartnerId);
        try {
            orderRepo.save(order);
        } catch (Exception e) {
            log.error("error in OrderService, assignOrderToDeliveryPartner() : " + e);
            throw new RuntimeException(e);
        }

    }


    public String markOrderAsOutForDelivery(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("there is no new order with 'order-id = " + orderId + "'"));

        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException("If the order is READY, only then you can pickup the order");
        }

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        try {
            orderRepo.save(order);
            return "Order is out for delivery.";
        } catch (Exception e) {
            log.error("error in OrderService, orderPicked() : " + e);
            throw new RuntimeException(e);
        }
    }

    public String markOrderAsArrived(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("there is no new order with 'order-id = " + orderId + "'"));

        if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
            throw new IllegalStateException("You cannot mark it as arrived if the order is not out for delivery");
        }

        order.setStatus(OrderStatus.ARRIVED);
        try {
            orderRepo.save(order);
            return "Order is arrived.";
        } catch (Exception e) {
            log.error("error in OrderService, orderArrived() : " + e);
            throw new RuntimeException(e);
        }
    }

    public String markOrderAsDelivered(Integer orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("there is no new order with 'order-id = " + orderId + "'"));

        if (order.getStatus() != OrderStatus.ARRIVED) {
            throw new IllegalStateException("You cannot mark it as delivered if the order is not arrived");
        }

        order.setStatus(OrderStatus.DELIVERED);
        try {
            orderRepo.save(order);
            return "Order is delivered successfully.";
        } catch (Exception e) {
            log.error("error in OrderService, orderDelivered() : " + e);
            throw new RuntimeException(e);
        }
    }

    public List<NewOrdersResponse> getNewOrdersList() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();
        Restaurant restaurant = restaurantRepo.findByUserUserId(userId);
        if (restaurant == null || !restaurant.getActive()) {
            throw new ResourceNotFoundException("Restaurant profile not created yet!");
        }

        Integer restaurantId = restaurant.getRestaurantId();
        ;

        List<Order> pendingOrders = orderRepo.findByRestaurantRestaurantIdAndStatus(restaurantId, OrderStatus.PLACED);

        if (pendingOrders.isEmpty()) {
            throw new ResourceNotFoundException("You don't have any new orders...");
        }

        ArrayList<NewOrdersResponse> listOfNewOrders = new ArrayList<>();
        for (Order order : pendingOrders) {
            Integer orderId = order.getOrderId();

            ArrayList<OrderItem> rawItems = orderItemRepo.findByOrderOrderId(orderId);

            // just basic mapping for intended response
            List<DishResponse> items = rawItems.stream()
                    .map((i) -> {
                        DishResponse dish = new DishResponse();
                        dish.setDishName(i.getDishName());
                        dish.setQty(i.getQty());
//                        dish.setUserId(i.getUser().getUserId());
//                        dish.setUserName(i.getUser().getName());
                        dish.setAddress(order.getAddress());

                        return dish;
                    })
                    .toList();

            NewOrdersResponse newOrder = new NewOrdersResponse();
            newOrder.setOrderId(orderId);
            newOrder.setUserId(rawItems.get(0).getUser().getUserId());
            newOrder.setUserName(rawItems.get(0).getUserName());
            newOrder.setStatus(order.getStatus());
            newOrder.setItems(items);

            listOfNewOrders.add(newOrder);
        }
        return listOfNewOrders;
    }

    public CurrentOrderResponse getCurrentOrders() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        List<Order> orders = orderRepo.findByUserId(userId);

        if (orders.isEmpty()) {
            throw new ResourceNotFoundException("You don't have any live orders.");
        }

        List<CurrentOrder> listOfOrders = new ArrayList<>();
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

            CurrentOrder response = new CurrentOrder();
            response.setOrderId(orderId);
            response.setStatus(order.getStatus());
            response.setRestaurantId(order.getRestaurant().getRestaurantId());
            response.setRestaurantName(order.getRestaurant().getName());
            response.setItems(orderItems);
            response.setTotal(order.getTotal());

            listOfOrders.add(response);
        }

        Double netTotal = 0.0;
        for (CurrentOrder order : listOfOrders) {
            netTotal += order.getTotal();
        }

        CurrentOrderResponse res = new CurrentOrderResponse();
        res.setOrders(listOfOrders);
        res.setNetTotal(netTotal);
        return res;
    }

    public List<PastOrder> getOrderHistoryOfCurrentUser() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        List<Order> rawOrderList = orderRepo.findByUserIdOrderByOrderIdAsc(userId);


        List<Order> filteredOrderlist = rawOrderList.stream().filter(o -> o.getStatus() == OrderStatus.CANCELLED || o.getStatus() == OrderStatus.REJECTED || o.getStatus() == OrderStatus.DELIVERED).toList();

        if (filteredOrderlist.isEmpty()) {
            throw new ResourceNotFoundException("You don't have any past Order history.");
        }

        return filteredOrderlist.stream()
                .map((order) -> {
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

                    PastOrder orderResponse = new PastOrder();
                    orderResponse.setOrderId(orderId);
                    orderResponse.setStatus(order.getStatus());
                    orderResponse.setRestaurantId(order.getRestaurant().getRestaurantId());
                    orderResponse.setRestaurantName(order.getRestaurant().getName());
                    orderResponse.setItems(orderItems);
                    orderResponse.setTotal(order.getTotal());

                    return orderResponse;
                }).toList();
    }

    public List<CurrentOrder> getLiveOrderOfCurrentUser() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        List<Order> rawOrderList = orderRepo.findByUserIdAndRestaurantActiveOrderByOrderIdAsc(userId, true);

        List<Order> filteredOrderList = rawOrderList.stream().filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.REJECTED && o.getStatus() != OrderStatus.CANCELLED).toList();
        if (filteredOrderList.isEmpty()) {
            throw new ResourceNotFoundException("You don't have any ongoing live-orders.");
        }

        return filteredOrderList.stream()
                .map((order) -> {
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

                    CurrentOrder orderResponse = new CurrentOrder();
                    orderResponse.setOrderId(orderId);
                    orderResponse.setStatus(order.getStatus());
                    orderResponse.setRestaurantId(order.getRestaurant().getRestaurantId());
                    orderResponse.setRestaurantName(order.getRestaurant().getName());
                    orderResponse.setItems(orderItems);
                    orderResponse.setTotal(order.getTotal());

                    return orderResponse;
                }).toList();
    }

    public List<LiveOrder> getLiveOrdersOfThisRestaurant() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();
        Integer restaurantId = restaurantRepo.findByUserUserId(userId).getRestaurantId();

        List<Order> rawOrderList = orderRepo.findByRestaurantRestaurantId(restaurantId);
        List<Order> filteredOrderList = rawOrderList.stream().filter(o -> o.getStatus() != OrderStatus.DELIVERED && o.getStatus() != OrderStatus.REJECTED && o.getStatus() != OrderStatus.CANCELLED && o.getStatus() != OrderStatus.PLACED).toList();

        List<LiveOrder> liveOrders = filteredOrderList.stream()
                .map((order) -> {
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

                    LiveOrder orderResponse = new LiveOrder();
                    orderResponse.setOrderId(orderId);
                    orderResponse.setStatus(order.getStatus());
                    orderResponse.setCustomerId(order.getUserId());
                    AppUser user = userRepo.findById(order.getUserId()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
                    orderResponse.setCustomerName(user.getName()); // customer name
                    orderResponse.setItems(orderItems);
                    orderResponse.setTotal(order.getTotal());

                    return orderResponse;
                }).toList();

        if (liveOrders.isEmpty()) {
            throw new ResourceNotFoundException("You dont have any live ongoing orders.Thankyou");
        }

        return liveOrders;

    }

    public DeliveryPartnerNewOrderResponse getNewOrdersListOfCurrentDeliveryPartner() {
        Authentication authObj = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userDetails = (UserPrincipal) authObj.getPrincipal();
        Integer userId = userDetails.getUserId();

        DeliveryPartner deliveryPartner = deliveryPartnerRepo.findByUserUserId(userId);
        if (deliveryPartner == null || !deliveryPartner.getActive()) {
            throw new ResourceNotFoundException("Delivery partner deoes'nt exist");
        }

        Integer deliveryPartnerId = deliveryPartner.getDeliveryPartnerId();

        Order newOrder = orderRepo.findByDeliveryPartnerIdAndStatus(deliveryPartnerId, OrderStatus.READY);

        if (newOrder == null) {
            throw new ResourceNotFoundException("You don't have any new orders...");
        }

        return getDeliveryPartnerNewOrderResponse(newOrder);
    }

    private DeliveryPartnerNewOrderResponse getDeliveryPartnerNewOrderResponse(Order newOrder) {
        DeliveryPartnerNewOrderResponse newOrderResponse = new DeliveryPartnerNewOrderResponse();
        newOrderResponse.setOrderId(newOrder.getOrderId());
        newOrderResponse.setStatus(newOrder.getStatus());
        newOrderResponse.setRestaurantId(newOrder.getRestaurant().getRestaurantId());
        newOrderResponse.setRestaurantName(newOrder.getRestaurant().getName());
        newOrderResponse.setRestaurantAddress(newOrder.getRestaurant().getStreetAddress());
        newOrderResponse.setEarnings(35.0);
        return newOrderResponse;
    }
}