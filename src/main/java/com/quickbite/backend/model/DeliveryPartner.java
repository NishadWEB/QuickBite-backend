package com.quickbite.backend.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "delivery_partners")
@Data
public class DeliveryPartner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer deliveryPartnerId;

    @OneToOne
    private AppUser user;

    private Boolean active;
}
