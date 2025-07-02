package com.project.task_manager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Informe seu e-mail.")
    private String email;

    @NotBlank(message = "Informa sua senha.")
    private String password;
}
