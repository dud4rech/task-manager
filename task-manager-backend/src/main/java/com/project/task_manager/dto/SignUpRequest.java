package com.project.task_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotNull(message = "Nome não pode ser nulo.")
    @NotBlank(message = "Informe um nome.")
    private String name;

    @NotNull(message = "Username não pode ser nulo.")
    @NotBlank(message = "Informe um username.")
    private String username;

    @NotNull(message = "Senha não pode ser nula.")
    @NotBlank(message = "Informe uma senha.")
    private String password;
}
