package com.quickbite.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ErrResponse {
    private String message;
    private LocalDateTime time;
}
