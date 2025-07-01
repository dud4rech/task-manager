package com.example.task_manager_mobile.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.task_manager_mobile.databinding.ActivityLoginBinding;
import com.example.task_manager_mobile.dto.AuthResponse;
import com.example.task_manager_mobile.infrastructure.SessionManager;
import com.example.task_manager_mobile.requests.BaseApiCaller;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private SessionManager sessionManager;
    private BaseApiCaller baseApiCaller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        // Esconde a ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        this.baseApiCaller = new BaseApiCaller();
        this.sessionManager = new SessionManager(getApplicationContext());

        if (!TextUtils.isEmpty(sessionManager.getAuthToken())) {
            this.navigateToMainActivity();
        }

        this.setupClickListeners();
    }
    private void setupClickListeners() {
        binding.btnLogin.setOnClickListener(v -> {
            handleLoginAttempt();
        });

        binding.tvCreateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void handleLoginAttempt() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            binding.etEmail.setError("O e-mail é obrigatório");
            binding.etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("A senha é obrigatória");
            binding.etPassword.requestFocus();
            return;
        }

        Toast.makeText(this, "Iniciando login...", Toast.LENGTH_LONG).show();

        baseApiCaller.login(email, password, new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String result) {
                // Roda na thread principal para poder atualizar a UI
                runOnUiThread(() -> {
                    try {
                        Gson gson = new Gson();
                        AuthResponse response = gson.fromJson(result, AuthResponse.class);
                        String token = response.getToken();
                        sessionManager.saveAuthToken(token);

                        Toast.makeText(LoginActivity.this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                        navigateToMainActivity();
                    } catch (JsonSyntaxException e) {
                        Toast.makeText(LoginActivity.this, "Erro ao processar a resposta do servidor.", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    Toast.makeText(LoginActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}