package com.quickbite.backend.dto.delivery_DTO;

import com.quickbite.backend.dto.order_DTO.DishResponse;
import com.quickbite.backend.enums.OrderStatus;
import lombok.Data;

import java.util.List;

@Data
public class DeliveryPartnerNewOrderResponse {
    private Integer orderId; // orderId
    private OrderStatus status;

    private Integer restaurantId;
    private String restaurantName;
    private String restaurantAddress;

    private Double earnings;
}
