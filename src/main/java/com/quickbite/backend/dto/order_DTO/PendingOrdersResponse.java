package com.quickbite.backend.dto.order_DTO;

import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.model.Dish;
import com.quickbite.backend.model.Order;
import com.quickbite.backend.model.Restaurant;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.List;

@Data
public class PendingOrdersResponse {
    private Integer orderId; // orderId
    private OrderStatus status;
    List<DishResponse> items;
}
