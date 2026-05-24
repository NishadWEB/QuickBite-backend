package com.quickbite.backend.A3_repo;

import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.model.Order;
import com.quickbite.backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;

public interface OrderRepo extends JpaRepository<Order, Integer> {
    ArrayList<Order> findByRestaurantRestaurantIdAndStatus(Integer restaurantId, OrderStatus orderStatus);
}
