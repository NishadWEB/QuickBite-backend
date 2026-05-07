package com.quickbite.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @Email(message = "invalid email format!")
    @NotBlank(message = "Invalid email or pass")
    private String email;

    @NotBlank(message = "Invalid email or pass")
    private String password;
}
