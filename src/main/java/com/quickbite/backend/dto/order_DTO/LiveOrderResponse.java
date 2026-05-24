package com.quickbite.backend.dto.order_DTO;

import com.quickbite.backend.enums.OrderStatus;
import com.quickbite.backend.model.Restaurant;
import jakarta.persistence.ManyToOne;
import lombok.Data;

import java.util.List;

@Data
public class LiveOrderResponse {
    private Integer orderId;
    private OrderStatus status;

    private Integer restaurantId;
    private String restaurantName;

    private List<Item> items;
    private Double total;
}
