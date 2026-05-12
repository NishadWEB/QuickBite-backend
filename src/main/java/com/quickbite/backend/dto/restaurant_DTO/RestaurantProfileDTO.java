package com.quickbite.backend.dto.restaurant_DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;

@Data
public class RestaurantProfileDTO {

    @NotBlank(message = "restaurant name cannot be empty!")
    private String name;

    @NotBlank(message = "description cannot be empty!")
    private String description;

    @NotBlank(message = "cuisine-type cannot be empty!")
    private String cuisineType;

    @NotBlank(message = "street-address cannot be empty!")
    private String streetAddress;

    @NotBlank(message = "city name cannot be empty!")
    private String city;

    @NotBlank(message = "state cannot be emtpy" )
    private String state;

    @NotBlank(message = "pin-code cannot be emtpy")
    private String pinCode;

    @NotNull(message = "Restaurant name cannot ne empty")
    @DateTimeFormat(pattern = "hh:mm:ss a")
    private LocalTime openingTime;

    @NotNull(message = "Closing time cannot be empty")
    @DateTimeFormat(pattern = "hh:mm:ss a")
    private LocalTime closingTime;

    @NotNull(message = "Average preparation time cannot be empty")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime avgPreparationTime;

    @NotNull(message = "Delivery time cannot be empty.")
    @DateTimeFormat(pattern = "HH:mm:ss")
    private LocalTime deliveryTime;

    @NotNull(message = "Logo cannot be empty")
    private MultipartFile logo;

    @NotBlank(message = "Cannot create a Restaurant profile without the FSSAI-License.")
    private String FSSAILicenseNumber;

    private Double rating;
}
