package com.quickbite.backend.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cart {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cartId;

    @ManyToOne
    private AppUser user; // dynamic fetching from jwt

    @ManyToOne
    private Restaurant restaurant;

    @ManyToOne
    private Dish dish;

    private String dishName;

    private Double dishPrice;
    private Integer qty;
    private Double total;
}
