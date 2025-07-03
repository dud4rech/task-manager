package com.example.task_manager_mobile.requests;
import androidx.annotation.NonNull;

import com.example.task_manager_mobile.dto.ShareTaskRequest;
import com.example.task_manager_mobile.dto.Task;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Classe base para realizar chamadas a uma API REST usando OkHttp.
 * Lida com a construção de requisições, execução e callbacks.
 */
public class BaseApiCaller {

    public static final String BASE_URL = "http://192.168.0.20:8080/";

    private static final OkHttpClient client = new OkHttpClient();
    private static final Gson gson = new Gson();
    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    /**
     * Interface para callbacks de chamadas de API, retornando o resultado
     * ou um erro de forma assíncrona.
     *
     * @param <T> O tipo do objeto esperado na resposta.
     */
    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    // --- Métodos de Autenticação ---

    /**
     * Cadastra um novo usuário.
     * Endpoint: POST /auth/signup
     */
    public void signUp(String username, String password, String email, final ApiCallback<String> callback) {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("username", username);
        bodyMap.put("password", password);
        bodyMap.put("name", email);
        String jsonBody = gson.toJson(bodyMap);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "auth/signup")
                .post(requestBody)
                .build();

        executeCall(request, callback);
    }

    /**
     * Realiza o login de um usuário.
     * Nota: A coleção Postman usa GET com corpo, o que é incomum.
     * OkHttp suporta isso usando .method("GET", body).
     * Endpoint: POST /auth/login
     */
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

    // --- Métodos de Usuários (Users) ---

    /**
     * Lista todos os usuários.
     * Endpoint: GET /users
     */
    public void listUsers(String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "users")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        executeCall(request, callback);
    }

    /**
     * Busca um usuário pelo ID.
     * Endpoint: GET /users/{id}
     */
    public void getUserById(String userId, String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "users/" + userId)
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        executeCall(request, callback);
    }

    /**
     * Atualiza um usuário existente.
     * Endpoint: PUT /users/{id}
     */
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

    /**
     * Deleta um usuário pelo ID.
     * Endpoint: DELETE /users/{id}
     */
    public void deleteUser(String userId, String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "users/" + userId)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        executeCall(request, callback);
    }

    /**
     * Faz upload da foto de perfil de um usuário.
     * Endpoint: POST /users/{id}/upload-profile-picture
     */
    public void uploadProfilePicture(String userId, String base64Image, String token, final ApiCallback<String> callback) {
        Map<String, String> bodyMap = new HashMap<>();
        bodyMap.put("image", base64Image);
        String jsonBody = gson.toJson(bodyMap);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);
        Request request = new Request.Builder()
                .url(BASE_URL + "users/" + userId + "/upload-profile-picture")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        executeCall(request, callback);
    }


    // --- Métodos de Tarefas (Tasks) ---

    /**
     * Lista todas as tarefas. O usuário será pego pelo backend com base na autenticação
     * Endpoint: GET /tasks
     */
    public void listTasks(String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "tasks")
                .addHeader("Authorization", "Bearer " + token)
                .get()
                .build();

        executeCall(request, callback);
    }

    /**
     * Cria uma nova tarefa.
     * Endpoint: POST /tasks
     */
    public void createTask(Task newTask, String token, final ApiCallback<String> callback) {
        String jsonBody = gson.toJson(newTask);

        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "tasks")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
                .build();

        executeCall(request, callback);
    }

    public void updateTask(String taskId, Task taskToUpdate, String token, final ApiCallback<String> callback) {
        String jsonBody = gson.toJson(taskToUpdate);
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "tasks/" + taskId)
                .addHeader("Authorization", "Bearer " + token)
                .put(requestBody)
                .build();

        executeCall(request, callback);
    }

    /**
     * Deleta uma tarefa pelo ID.
     * Endpoint: DELETE /tasks/{id}
     */
    public void deleteTask(String taskId, String token, final ApiCallback<String> callback) {
        Request request = new Request.Builder()
                .url(BASE_URL + "tasks/" + taskId)
                .addHeader("Authorization", "Bearer " + token)
                .delete()
                .build();

        executeCall(request, callback);
    }

    /**
     * Compartilha uma tarefa com outros usuários.
     * Endpoint: POST /tasks/{id}/share
     */
    public void shareTask(String taskId, List<String> usernames, String token, final ApiCallback<String> callback) {
        ShareTaskRequest shareRequest = new ShareTaskRequest(usernames);
        String jsonBody = gson.toJson(shareRequest);
        RequestBody requestBody = RequestBody.create(jsonBody, JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "tasks/" + taskId + "/share")
                .addHeader("Authorization", "Bearer " + token)
                .post(requestBody)
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

    public void getSharedUsersForTask(String taskId, String token, final ApiCallback<String> callback) {
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

    // --- Executor Genérico de Chamadas ---

    /**
     * Executa a requisição de forma assíncrona e chama o callback apropriado.
     * @param request O objeto Request a ser executado.
     * @param callback O callback para lidar com a resposta.
     */
    private void executeCall(Request request, final ApiCallback<String> callback) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                // Erro de rede ou I/O
                callback.onError("Falha na requisição: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                    if (!response.isSuccessful()) {
                        // Erro do servidor (ex: 404, 500)
                        String errorBody = responseBody != null ? responseBody.string() : "Corpo da resposta vazio";
                        callback.onError("Erro na resposta do servidor: " + response.code() + " - " + errorBody);
                        return;
                    }

                    // Sucesso
                    String result = responseBody != null ? responseBody.string() : "";
                    callback.onSuccess(result);
                }
            }
        });
    }
}