package com.quickbite.backend.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AddToCartDTO {
    private Integer restaurantId;
    private Integer dishId;

    private String dishName;

    private Double dishPrice;
    private Integer qty;
    private MultipartFile dishImage;
    private Double total;
}
