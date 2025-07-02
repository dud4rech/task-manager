package com.example.task_manager_mobile.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// Importe a classe de binding gerada para o seu novo layout
import com.example.task_manager_mobile.databinding.ActivitySignUpBinding;
import com.example.task_manager_mobile.dto.AuthResponse;
import com.example.task_manager_mobile.infrastructure.SessionManager;
import com.example.task_manager_mobile.requests.BaseApiCaller;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private SessionManager sessionManager;
    private BaseApiCaller baseApiCaller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Esconde a ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        this.baseApiCaller = new BaseApiCaller();
        this.sessionManager = new SessionManager(getApplicationContext());

        setupClickListeners();
    }

    private void setupClickListeners() {
        binding.btnRegister.setOnClickListener(v -> handleRegistration());

        // Listener para o link de Login
        binding.tvLoginLink.setOnClickListener(v -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleRegistration() {
        String name = binding.etName.getText().toString().trim();
        String username = binding.etUsername.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("O nome é obrigatório");
            binding.etName.requestFocus();
            return;
        }

        // Não permite espaços nem caracteres especiais
        if (!username.matches("^[a-zA-Z0-9]+$")) {
            binding.etUsername.setError("O nome de usuário deve conter apenas letras e números, sem espaços.");
            binding.etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(name)) {
            binding.etName.setError("O nome é obrigatório");
            binding.etName.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("A senha é obrigatória");
            binding.etPassword.requestFocus();
            return;
        }

        // Validação específica da senha
        if (password.length() < 8) {
            binding.etPassword.setError("A senha deve ter pelo menos 8 caracteres");
            binding.etPassword.requestFocus();
            return;
        }

        BaseApiCaller baseApiCaller = new BaseApiCaller();

        Toast.makeText(this, "Iniciando cadastro...", Toast.LENGTH_LONG).show();

        baseApiCaller.signUp(username, password, name, new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                runOnUiThread(() -> {
                    try {
                        Gson gson = new Gson();
                        AuthResponse response = gson.fromJson(result, AuthResponse.class);
                        String token = response.getToken();
                        sessionManager.saveAuthToken(token);

                        Toast.makeText(SignUpActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                        navigateToMainActivity();
                    } catch (JsonSyntaxException e) {
                        Toast.makeText(SignUpActivity.this, "Erro ao processar a resposta do servidor.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(SignUpActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}