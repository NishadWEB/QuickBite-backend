package com.quickbite.backend.model;

import com.quickbite.backend.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "order_items")
@Data
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderItemId;

    @ManyToOne
    private Order order; // orderId

    @ManyToOne
    private AppUser user;
    private String userName;

    @ManyToOne
    private Restaurant restaurant;
    private String restaurantName;

    @ManyToOne
    private Dish dish;
    private String dishName;
    private Double price;
    private Integer qty;
    private Double total;
}
