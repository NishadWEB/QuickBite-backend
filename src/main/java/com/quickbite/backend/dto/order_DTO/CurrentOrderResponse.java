package com.quickbite.backend.dto.order_DTO;

import lombok.Data;

import java.util.List;

@Data
public class CurrentOrderResponse {
    private List<CurrentOrder> orders;
    private Double netTotal;
}
