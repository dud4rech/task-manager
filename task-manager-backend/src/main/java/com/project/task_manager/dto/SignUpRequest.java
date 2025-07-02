package com.project.task_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotNull(message = "Nome de usuário não pode ser nulo.")
    @NotBlank(message = "Informe um nome de usuário.")
    private String username;

    @NotNull(message = "E-mail não pode ser nulo.")
    @NotBlank(message = "Informe um e-mail.")
    @Email(message = "E-mail inválido.")
    private String email;

    @NotNull(message = "Senha não pode ser nula.")
    @NotBlank(message = "Informe uma senha.")
    private String password;
}
