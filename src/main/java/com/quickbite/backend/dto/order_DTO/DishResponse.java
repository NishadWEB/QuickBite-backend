package com.quickbite.backend.dto.order_DTO;

import lombok.Data;

@Data
public class DishResponse {
    private String dishName;
    private Integer qty;
    // customers
    private Integer userId;
    private String userName;
    private String address;
}
