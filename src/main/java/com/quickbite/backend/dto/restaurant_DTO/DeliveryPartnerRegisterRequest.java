package com.quickbite.backend.dto.restaurant_DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class DeliveryPartnerRegisterRequest {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "email cannot be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "invalid password")
    private String password;

    @NotBlank(message = "phone number cannot be empty")
    @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invaid phone number")
    private String phone;
}
