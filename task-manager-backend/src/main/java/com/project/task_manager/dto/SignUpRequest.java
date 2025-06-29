package com.project.task_manager.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SignUpRequest {

    @NotNull(message = "Username can't be null.")
    @NotBlank(message = "Username is required.")
    private String username;

    @NotNull(message = "Email can't be null.")
    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email.")
    private String email;

    @NotNull(message = "Password can't be null.")
    @NotBlank(message = "Password is required.")
    private String password;
}
