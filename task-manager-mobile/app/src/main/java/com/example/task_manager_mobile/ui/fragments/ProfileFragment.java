package com.example.task_manager_mobile.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.auth0.android.jwt.JWT;
import com.example.task_manager_mobile.R;
import com.example.task_manager_mobile.ui.activities.EditProfileActivity;
import com.example.task_manager_mobile.ui.activities.LoginActivity;
import com.example.task_manager_mobile.databinding.FragmentProfileBinding;
import com.example.task_manager_mobile.dto.User;
import com.example.task_manager_mobile.infrastructure.SessionManager;
import com.example.task_manager_mobile.requests.BaseApiCaller;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private SessionManager sessionManager;
    private BaseApiCaller baseApiCaller;
    private User currentUser;

    private final ActivityResultLauncher<Intent> editProfileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Toast.makeText(getContext(), "Perfil atualizado.", Toast.LENGTH_SHORT).show();
                    loadUserData();
                }
            });

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());
        baseApiCaller = new BaseApiCaller();

        loadUserData();

        binding.btnLogout.setOnClickListener(v -> logout());
        binding.btnEditProfile.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(getContext(), EditProfileActivity.class);

                intent.putExtra(EditProfileActivity.EXTRA_USER_ID, currentUser.getId());

                editProfileLauncher.launch(intent);
            } else {
                Toast.makeText(getContext(), "Aguarde os dados do usuário carregarem.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserData() {
        showLoading(true);

        String token = sessionManager.getAuthToken();
        if (token == null || token.isEmpty()) {
            Toast.makeText(getContext(), "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
            this.logout();
            showLoading(false);
            return;
        }

        JWT jwt = new JWT(token);
        String userId = jwt.getSubject();

        if (userId == null) {
            Toast.makeText(getContext(), "Erro ao ler informações do usuário.", Toast.LENGTH_SHORT).show();
            showLoading(false);
            return;
        }

        baseApiCaller.getUserById(userId, token, new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String jsonResult) {
                if (getActivity() == null) return;

                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    try {
                        currentUser = new Gson().fromJson(jsonResult, User.class);
                        populateUI();
                    } catch (JsonSyntaxException e) {
                        Toast.makeText(getContext(), "Falha ao processar dados do perfil.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String message) {
                if (getActivity() == null) return;
                getActivity().runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(getContext(), "Erro ao carregar perfil: " + message, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;

        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.groupProfileContent.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
    }

    private void populateUI() {
        binding.tvName.setText(currentUser.getName());
        binding.tvUserName.setText(currentUser.getUsername());
        binding.tvInfoUserName.setText(currentUser.getUsername());
        binding.tvInfoFullName.setText(currentUser.getName());

        String base64Image = currentUser.getProfilePicture();

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                binding.profileImage.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                binding.profileImage.setImageResource(R.drawable.baseline_person_24);
            }
        } else {
            binding.profileImage.setImageResource(R.drawable.baseline_person_24);
        }
    }

    private void logout() {
        sessionManager.clearAuthToken();
        Intent intent = new Intent(getContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}