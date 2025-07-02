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

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void register(@Valid SignUpRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .isActive(true)
                .build();

        userRepository.save(newUser);
    }

    @Transactional
    public void update(String email, Long id, User newUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!existingUser.getEmail().equals(email)) {
            throw new SecurityException("Você não está autorizado a atualizar este usuário.");
        }

        existingUser.setUsername(newUser.getUsername());
        existingUser.setEmail(newUser.getEmail());
        existingUser.setPasswordHash(passwordEncoder.encode(newUser.getPassword()));

        if (newUser.getProfilePicture() != null) {
            existingUser.setProfilePicture(newUser.getProfilePicture());
        }

        userRepository.save(existingUser);
    }

    @Transactional
    public void softDelete(String email, Long id) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Usuário não encontrado."));

        if (!existingUser.getEmail().equals(email)) {
            throw new SecurityException("Você não está autorizado a deletar este usuário.");
        }

        existingUser.setIsActive(false);
        userRepository.save(existingUser);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
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
