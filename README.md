# ChatApp-Springboot


A secure and real-time backend for ChatApp built with **Spring Boot 3.4.3**, using **JWT authentication**, **WebSockets + STOMP**, **JPA/Hibernate**, **OTP email service**, and **PostgreSQL** with **Docker Compose**.

---

## ⚙️ Tech Stack

- Spring Boot 3.4.2
- Spring Security + JWT
- WebSockets (STOMP)
- JPA + Hibernate ORM
- PostgreSQL (via Docker Compose)
- Email Service for OTP (SMTP)
- Lombok (to reduce boilerplate)
- Maven
-javaSE-17(JDK) or higher
---

## 🚀 Features

- User registration & JWT-based login
- Email OTP verification before account activation
- Real-time chat via WebSockets + STOMP
- Chat history stored in PostgreSQL
- Role-based authorization with Spring Security
- Secure WebSocket connection authentication
- Dockerized PostgreSQL database

---

## 🐳 Docker Setup for PostgreSQL

Start PostgreSQL with Docker Compose:

```bash
docker-compose up -d

```
⚙️ Configuration (src/main/resources/application.yml)
yaml

```
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/chatdb
    username: postgres
    password: postgres
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  mail:
    host: smtp.your-email.com
    port: 587
    username: your-email@example.com
    password: your-email-password
    
   ```
    
▶️ Running the Application

Use Eclipse or the terminal:

Using Maven (Terminal):

```bash
./mvnw clean install
./mvnw spring-boot:run
```
Or run the main class: ChatAppApplication.java inside Eclipse.


