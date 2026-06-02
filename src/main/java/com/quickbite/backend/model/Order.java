package com.quickbite.backend.model;

import com.quickbite.backend.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne
    private Restaurant restaurant;

    private Integer userId; // customer, (not restaurant)
    private Double total;

    private Integer deliveryPartnerId;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String address;
}
