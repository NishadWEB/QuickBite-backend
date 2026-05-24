package com.quickbite.backend.dto.order_DTO;

import lombok.Data;

@Data
public class Item {
    private Integer dishId;
    private String dishName;

    private Integer qty;
    private Double price;
    private Double total;
}
