package com.project.task_manager.controller;

import com.project.task_manager.model.User;
import com.project.task_manager.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<User> listAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> listUserById(@PathVariable Long id) {
        Optional<User> user = userService.findById(id);

        if (user.isPresent()) {
            return ResponseEntity.ok(user.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Usuário não encontrado."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id, @RequestBody User user) {
        try {
            userService.update(userDetails.getUsername(), id, user);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("Você não está autorizado a atualizar este usuário.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Username já existe ou usuário não encontrado.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> softDeleteUser(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long id) {
        try {
            userService.softDelete(userDetails.getUsername(), id);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body("Você não está autorizado a deletar este usuário.");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body("Username já existe ou usuário não encontrado.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/upload-profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String image = body.get("image");

        try {
            userService.saveProfilePicture(id, image);
            return ResponseEntity.ok("Imagem enviada com sucesso.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }
}
