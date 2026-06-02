package com.quickbite.backend.dto.delivery_DTO;

import com.quickbite.backend.dto.order_DTO.Item;
import com.quickbite.backend.enums.OrderStatus;
import lombok.Data;

import java.util.List;

@Data
public class DeliveryPartnerLiveOrderResponse {
    private Integer orderId;
    private OrderStatus status;

    private Integer restaurantId;
    private String restaurantName;

    private Integer customerId;
    private String customerName;
    private String customerAddress;

    private List<Item> items;
    private Double total;
}
