package com.quickbite.backend.dto.order_DTO;

import lombok.Data;

import java.util.List;

@Data
public class CurrentOrderResponse {
    private List<LiveOrderResponse> orders;
    private Double netTotal;
}
