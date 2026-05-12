package com.quickbite.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Table(name = "restaurants")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer restaurantId;

    private String name;

    private String description;

    private String cuisineType;

    private String streetAddress;

    private String city;

    private String state;

    private String pinCode;

    private LocalTime openingTime;

    private LocalTime closingTime;

    private LocalTime avgPreparationTime;

    private LocalTime deliveryTime;

    @Lob
    private byte[] logo;

    @Column(unique = true)
    private String FSSAILicenseNumber;

    private Double rating;
}
