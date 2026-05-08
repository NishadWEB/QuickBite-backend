package com.quickbite.backend.dto;

import lombok.Data;

@Data
public class AddToCartDTO {
    private Integer restaurantId;
    private Integer dishId;

    private String dishName;

    private Double dishPrice;
    private Integer qty;
    private Double total;
}
