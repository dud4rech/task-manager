package com.project.task_manager.service;

import com.project.task_manager.dto.SignUpRequest;
import com.project.task_manager.model.User;
import com.project.task_manager.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void register(@Valid SignUpRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username já está em uso.");
        }

        User newUser = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        userRepository.save(newUser);
    }

    @Transactional
    public void update(String username, Long id, User newUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!existingUser.getUsername().equals(username)) {
            throw new SecurityException("Você não está autorizado a atualizar este usuário.");
        }

        existingUser.setName(newUser.getName());
        existingUser.setUsername(newUser.getUsername());

        if (newUser.getProfilePicture() != null) {
            existingUser.setProfilePicture(newUser.getProfilePicture());
        }

        userRepository.save(existingUser);
    }

    @Transactional
    public void softDelete(String username, Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!existingUser.getUsername().equals(username)) {
            throw new SecurityException("Você não está autorizado a deletar este usuário.");
        }

        existingUser.setIsActive(false);
        userRepository.save(existingUser);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles("USER")
                .build();
    }

    @Transactional
    public void saveProfilePicture(Long userId, String image) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado."));

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Por favor, envie uma imagem.");
        }

        user.setProfilePicture(image);
        userRepository.save(user);
    }}
