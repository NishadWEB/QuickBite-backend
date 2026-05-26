package com.quickbite.backend.enums;

public enum OrderStatus {
    // for Customer
    PLACED,
    CANCELLED,

    // for Restaurant
    ACCEPTED,
    REJECTED,
    READY,

    // for delivery-boy
    OUT_FOR_DELIVERY,
    ARRIVED,
    DELIVERED
}
