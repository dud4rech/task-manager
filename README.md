# 📱 Task Manager - Projeto para Aula de Programação para Dispositivos Móveis

Este projeto é uma API backend em **Java** com **Spring Boot** e **segurança JWT**, desenvolvido para a disciplina de **Programação para Dispositivos Móveis**. Ele gerencia usuários e tarefas com autenticação segura e funcionalidades CRUD.

---

## 🛠️ Tecnologias Utilizadas

- **Java 17+**
- **Spring Boot** (API REST)
- **Maven** (build e dependências)
- **Spring Security + JWT** (autenticação e autorização)
- Banco de Dados (H2 para teste ou MySQL/PostgreSQL em produção)
- JWT usando **ID do usuário** para maior segurança

---

## ✨ Funcionalidades

- Cadastro e login com token JWT
- Tokens baseados no **ID do usuário** (não quebram se o username mudar)
- Rotas protegidas para acesso autenticado
- CRUD de tarefas (criar, editar, excluir, compartilhar)
- Controle de acesso entre dono e usuários compartilhados
- Soft delete e bloqueio de acesso para usuários desativados
