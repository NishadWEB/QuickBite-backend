package com.quickbite.backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class Qty {
    @NotNull(message = "qty must be at-least 1")
    @Positive(message = "qty must be at-least 1")
    private Integer qty;
}
