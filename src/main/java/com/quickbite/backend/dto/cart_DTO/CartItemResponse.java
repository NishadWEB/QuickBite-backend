package com.quickbite.backend.dto.cart_DTO;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.quickbite.backend.model.AppUser;
import com.quickbite.backend.model.Dish;
import com.quickbite.backend.model.Restaurant;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
public class CartItemResponse {
    private Integer cartId;

    private Integer userId; // dynamic fetching from jwt

    private Integer restaurantId;

    private Integer dishId;
    private String dishName;
    private byte[] dishImage;

    private Double dishPrice;
    private Integer qty;
    private Double total;
}
