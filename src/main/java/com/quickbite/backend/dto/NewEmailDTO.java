package com.quickbite.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NewEmailDTO {
    @Email(message = "Invalid email format")
    @NotBlank(message = "new email cannot be empty")
    private String newEmail;
}
