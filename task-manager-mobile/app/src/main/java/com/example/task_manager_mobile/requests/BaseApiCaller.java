package com.example.task_manager_mobile.requests;
import androidx.annotation.NonNull;

import com.example.task_manager_mobile.dto.CreateTaskRequest;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class BaseApiCaller {

    public static final String BASE_URL = "http://192.168.0.28:8080/";
    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    public void signUp(String username, String password, String email, String profilePicture, final ApiCallback<String> callback) {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", username);
        bodyMap.put("password", password);
        bodyMap.put("name", email);
        bodyMap.put("profilePicture", profilePicture);
        String jsonBody = gson.toJson(bodyMap);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "auth/signup")
                .post(requestBody)
                .build();

        executeCall(request, callback);
    }

    public void login(String username, String password, final ApiCallback<String> callback) {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", username);
        bodyMap.put("password", password);
        String jsonBody = gson.toJson(bodyMap);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "auth/login")
                .method("POST", requestBody)
                .build();

        executeCall(request, callback);
    }

    public void getUserById(String userId, String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "users/" + userId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        executeCall(request, callback);
    }

    public void updateUser(String userId, String username, String name, String profilePicture, String token, final ApiCallback<String> callback) {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", username);
        bodyMap.put("name", name);
        bodyMap.put("profilePicture", profilePicture);
        String jsonBody = gson.toJson(bodyMap);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "users/" + userId)
                .addHeader("Authorization", "Bearer " + token)
                .put(requestBody)
                .build();

        executeCall(request, callback);
    }

    public void listTasks(String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "tasks")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        executeCall(request, callback);
    }

    public void createTask(CreateTaskRequest newTask, String token, final ApiCallback<String> callback) {
        String jsonBody = gson.toJson(newTask);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "tasks")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        executeCall(request, callback);
    }

    public void updateTask(String taskId, CreateTaskRequest taskToUpdate, String token, final ApiCallback<String> callback) {
        String jsonBody = gson.toJson(taskToUpdate);
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "tasks/" + taskId)
                .addHeader("Authorization", "Bearer " + token)
                .put(requestBody)
                .build();

        executeCall(request, callback);
    }

    public void deleteTask(String taskId, String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "tasks/" + taskId)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        executeCall(request, callback);
    }

    public void listAllUsers(String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "users")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();
        executeCall(request, callback);
    }

    public void getSharedUsersForTask(Long taskId, String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "tasks/" + taskId + "/shared-users")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();
        executeCall(request, callback);
    }

    public void getTaskById(String taskId, String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "tasks/" + taskId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        executeCall(request, callback);
    }

    private void executeCall(Request request, final ApiCallback<String> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Falha na requisição: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        String errorBody = responseBody != null ? responseBody.string() : "Corpo da resposta vazio";
                        callback.onError("Erro na resposta do servidor: " + response.code() + " - " + errorBody);
                        return;
                    }

                    String result = responseBody != null ? responseBody.string() : "";
                    callback.onSuccess(result);
                }
            }
        });
    }
}