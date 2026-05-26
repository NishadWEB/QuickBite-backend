package com.quickbite.backend.dto.cart_DTO;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class AddToCartDTO {
    private Integer restaurantId;
    private Integer dishId;

//    private String dishName;
//    private Double dishPrice;

    @NotNull(message = "qty must be atleast 1")
    @Positive(message = "qty must be atleat 1")
    private Integer qty;

    private Double total;
}
