package com.quickbite.backend.dto.restaurant_DTO;

import com.quickbite.backend.model.Restaurant;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DishDTO {
    private Integer dishId; // optional and used while fetching the dishes

    @NotBlank(message = "dish name cannot be empty")
    private String name;

    @NotNull(message = "dish price cannot be empty")
    private Integer price;

    private Double rating;

    @NotNull(message = "description cannot be empty")
    private String description;

    @NotNull(message = "dish image cannot be empty")
    private MultipartFile image;
}
