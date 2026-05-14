package com.quickbite.backend.dto.restaurant_DTO;

import com.quickbite.backend.model.AppUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalTime;

@Data
public class RestaurantResponseDTO {
    private Integer restaurantId;

    private String name;

    private String description;

    private String cuisineType;

    private String streetAddress;

    private String city;

    private String state;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private LocalTime avgPreparationTime;

    private LocalTime deliveryTime;

    private byte[] logo;

    private Double rating;
}
