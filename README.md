# 📱 Taskanduma - Projeto para Aula de Programação para Dispositivos Móveis

Este é um projeto é Full-Stack desenvolvido em **Java** com **Spring Boot** e **Android**, para a disciplina de **Programação para Dispositivos Móveis**. O **Taskanduma** É um aplicativo de gerenciamento de tarefas colaborativo. Ele permite aos usuários criar, visualizar, editar e excluir suas próprias tarefas, organizando-as por status. A principal funcionalidade é o compartilhamento de tarefas, que possibilita adicionar múltiplos participantes para colaborarem em um mesmo item. O sistema também inclui perfis de usuário personalizáveis, podendo adicionar informações pessoais mais objetivas e uma foto de perfil.


**Alunos:**
- André Eduardo Schmitz
- Ana Beatriz Martins da Silva
- Eduarda Stipp Rech
- Mariana Rossdeutrscher Waltrick Lima

---

## Como executar?

Garanta que você tenha os seguintes softwares instalados em sua máquina:

-   **JDK 17 ou superior:** Para executar o backend.
-   **PostgreSQL:** O banco de dados da aplicação.
-   **Android Studio:** Para executar o aplicativo mobile.
-   **Maven:** Para gerenciar as dependências e o build do backend.

---

### 1. Configuração do Backend (Servidor)

1.  **Crie o Banco de Dados:**
    -   Acesse seu terminal do PostgreSQL.
    -   Crie a database com o nome `task-manager`.
    ```sql
    CREATE DATABASE "task-manager";
    ```

2.  **Configure as Credenciais do Banco:**
    -   Abra o arquivo `/task-manager/task-manager-backend/src/main/resources/application.properties`.
    -   Altere as propriedades `spring.datasource.username` e `spring.datasource.password` com seu usuário e senha do PostgreSQL.
    ```properties
    #...
    spring.datasource.username=seu_usuario_postgres
    spring.datasource.password=sua_senha_postgres
    #...
    ```

3.  **Execute o servidor:**
    -   Você pode executar o projeto diretamente pela sua IDE ou via terminal usando o Maven:
    ```bash
    mvn spring-boot:run
    ```
    -   Por padrão, o servidor rodará na porta `8080`.

### 2. Configuração do Frontend (Aplicativo Android)

1.  **Configure o Endereço da API (BASE_URL):**
    -   Abra o arquivo `/task-manager/task-manager-mobile/app/src/main/java/com/example/task_manager_mobile/requests/BaseApiCaller.java`.
    -   Localize a constante `BASE_URL`.
    -   Insira o IP e porta no qual o servidor está rodando:
    ```java
    // ...
    public static final String BASE_URL = "http://192.168.0.28:8080/";
    // ...
    ```

2.  **Execute o Aplicativo:**
    -   Com o servidor backend rodando, execute o aplicativo no Android Studio para instalar no seu emulador ou dispositivo físico.