package com.quickbite.backend.A3_repo;

import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.ArrayList;
import java.util.List;

public interface OrderItemRepo extends JpaRepository<OrderItem, Integer> {
    ArrayList<OrderItem> findByOrderOrderId(Integer orderId);

    void deleteByRestaurantRestaurantId(Integer restaurantId);
}
