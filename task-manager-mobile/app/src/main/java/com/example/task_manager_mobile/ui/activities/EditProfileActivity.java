package com.example.task_manager_mobile.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.task_manager_mobile.R;
import com.example.task_manager_mobile.databinding.ActivityEditProfileBinding;
import com.example.task_manager_mobile.dto.User;
import com.example.task_manager_mobile.infrastructure.SessionManager;
import com.example.task_manager_mobile.requests.BaseApiCaller;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditProfileActivity extends AppCompatActivity {

    public static final String EXTRA_USER_ID = "user_id_extra";
    private ActivityEditProfileBinding binding;
    private User currentUser;
    private long userId;
    private BaseApiCaller baseApiCaller;
    private SessionManager sessionManager;
    private String newProfilePicBase64 = null;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        binding.editProfileImage.setImageURI(imageUri);
                        try {
                            newProfilePicBase64 = uriToBase64(imageUri);
                        } catch (IOException e) {
                            Toast.makeText(this, "Falha ao processar a imagem", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getLongExtra(EXTRA_USER_ID, -1);

        if (userId == -1) {
            Toast.makeText(this, "Erro ao carregar dados do usuário.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        baseApiCaller = new BaseApiCaller();
        sessionManager = new SessionManager(this);

        loadUserDetails();
        setupClickListeners();
    }

    private void loadUserDetails() {
        showLoading(true, true);
        String token = sessionManager.getAuthToken();
        baseApiCaller.getUserById(String.valueOf(userId), token, new BaseApiCaller.ApiCallback<String>() {
            @Override
            public void onSuccess(String user) {
                Gson gson = new Gson();
                currentUser = gson.fromJson(user, User.class);
                runOnUiThread(() -> {
                    populateInitialData();
                    showLoading(false, true);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false, true);
                    Toast.makeText(EditProfileActivity.this, "Erro ao carregar perfil: " + message, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void populateInitialData() {
        binding.etEditName.setText(currentUser.getName());
        binding.etEditUsername.setText(currentUser.getUsername());

        String base64Image = currentUser.getProfilePicture();

        if (base64Image != null && !base64Image.isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                binding.editProfileImage.setImageBitmap(decodedBitmap);
            } catch (IllegalArgumentException e) {
                binding.editProfileImage.setImageResource(R.drawable.baseline_person_24);
            }
        } else {
            binding.editProfileImage.setImageResource(R.drawable.baseline_person_24);
        }
    }

    private void setupClickListeners() {
        binding.editProfileImage.setOnClickListener(v -> openGallery());
        binding.btnCancel.setOnClickListener(v -> finish());
        binding.btnSave.setOnClickListener(v -> saveChanges());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveChanges() {
        String newName = binding.etEditName.getText().toString().trim();
        String newUsername = binding.etEditUsername.getText().toString().trim();
        String token = sessionManager.getAuthToken();
        String userId = String.valueOf(currentUser.getId());

        if (TextUtils.isEmpty(newUsername)) {
            binding.etEditUsername.setError("O nome de usuário não pode estar vazio");
            binding.etEditUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(newName)) {
            binding.etEditName.setError("O nome não pode estar vazio");
            binding.etEditName.requestFocus();
            return;
        }

        showLoading(true, false);

        String imageToSend = newProfilePicBase64 != null ? newProfilePicBase64 : currentUser.getProfilePicture();

         baseApiCaller.updateUser(userId, newUsername, newName, imageToSend, token, new BaseApiCaller.ApiCallback<String>() {
             @Override
             public void onSuccess(String updatedUser) {
                 runOnUiThread(() -> {
                     showLoading(false, false);
                     setResult(Activity.RESULT_OK);
                     finish();
                 });
             }

             @Override
             public void onError(String message) {
                 runOnUiThread(() -> {
                     showLoading(false, false);
                     Toast.makeText(EditProfileActivity.this, "Erro: " + message, Toast.LENGTH_LONG).show();
                 });
             }
         });
    }

    private void showLoading(boolean isLoading, boolean isInitialLoad) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);

        if (isInitialLoad) {
            binding.groupEditContent.setVisibility(isLoading ? View.INVISIBLE : View.VISIBLE);
        } else {
            binding.btnSave.setEnabled(!isLoading);
            binding.btnCancel.setEnabled(!isLoading);
        }
    }

    private String uriToBase64(Uri uri) throws IOException {
        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }
}