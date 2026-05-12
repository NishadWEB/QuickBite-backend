package com.quickbite.backend.dto.restaurant_DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class GetAllDishesDTO {
    private Integer dishId;
    private Integer restaurantId;

    private String name;

    private Integer price;

    private Double rating;

    private String description;

    private byte[] image;
}
