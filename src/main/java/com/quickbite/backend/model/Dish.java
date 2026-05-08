package com.quickbite.backend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dishes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dish {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer dishId;

    @ManyToOne
    private Restaurant restaurant;

    private String name;
    private Integer price;
    private Double rating;
    private String description;

    @Lob
    private byte[] image;
}
