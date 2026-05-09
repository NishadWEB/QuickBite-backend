package com.quickbite.backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordChangeDTO {
    @NotBlank(message = "Invalid password")
    private String oldPassword;

    @NotBlank(message = "Invalid password")
    private String newPassword;
}
