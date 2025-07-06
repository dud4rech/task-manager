package com.project.task_manager.controller;

import com.project.task_manager.dto.AuthResponse;
import com.project.task_manager.dto.LoginRequest;
import com.project.task_manager.dto.SignUpRequest;
import com.project.task_manager.service.JwtService;
import com.project.task_manager.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/signup")
    public ResponseEntity<?> signUp(@Valid @RequestBody SignUpRequest request) {
        try {
            userService.register(request);

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            var user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            var user = userService.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(new AuthResponse(token));
        } catch (BadCredentialsException | UsernameNotFoundException e) {
            return ResponseEntity
                    .status(401)
                    .body("Nome de usuário ou senha inválidos.");
        } catch (Exception e) {
            return ResponseEntity
                    .status(500)
                    .body("Um erro inesperado aconteceu.");
        }
    }
}
