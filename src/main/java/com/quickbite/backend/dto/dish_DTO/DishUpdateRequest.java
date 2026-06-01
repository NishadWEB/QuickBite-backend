package com.quickbite.backend.dto.dish_DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class DishUpdateRequest {
    private String name;
    private Integer price;
    private String description;
    private MultipartFile image;
}
