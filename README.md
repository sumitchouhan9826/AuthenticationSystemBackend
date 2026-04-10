# 🔐 Authentication System Backend

A secure and scalable **Authentication System Backend** built using **Spring Boot**.
Supports **JWT Authentication, OAuth2 (Google/GitHub), and Refresh Token-based session management**.

---

## 🚀 Features

* User Registration & Login
* Secure Authentication using JWT
* OAuth2 Login (Google / GitHub)
* Refresh Token Mechanism for session management
* Password Encryption using Spring Security (BCrypt)
* Role-Based Access Control (RBAC)
* RESTful APIs for seamless frontend integration
* Dockerized for easy deployment

---

## 🛠️ Tech Stack

* **Java**
* **Spring Boot**
* **Spring Security**
* **JWT (JSON Web Token)**
* **OAuth2 (Google, GitHub)**
* **Maven**
* **MySQL**
* **Docker**

---

## 📁 Project Structure

```
src/
├── controller/
├── service/
├── repository/
├── model/
├── security/
└── dto/
```

---

## ⚙️ Getting Started

### 1️⃣ Clone the repository

```bash
git clone https://github.com/your-username/AuthenticationSystemBackend.git
cd AuthenticationSystemBackend
```

---

### 2️⃣ Configure Database

Update `application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/your_db
spring.datasource.username=root
spring.datasource.password=your_password
```

---

### 3️⃣ Run the application

Using Maven wrapper:

```bash
./mvnw spring-boot:run
```

or on Windows:

```bash
mvnw.cmd spring-boot:run
```

---

### 4️⃣ Run with Docker (Optional)

```bash
docker build -t auth-backend .
docker run -p 8080:8080 auth-backend
```

---

## 🔗 API Endpoints

| Method | Endpoint                     | Description               |
| ------ | ---------------------------- | ------------------------- |
| POST   | /api/auth/register           | Register new user         |
| POST   | /api/auth/login              | Authenticate user         |
| POST   | /api/auth/refresh            | Generate new access token |
| GET    | /oauth2/authorization/google | Login with Google         |
| GET    | /oauth2/authorization/github | Login with GitHub         |

---

## 🔐 Security

* Passwords are encrypted using **BCrypt**
* JWT is used for stateless authentication
* Refresh tokens enable secure session persistence
* OAuth2 authentication integrated (Google, GitHub)
* Protected routes require a valid access token

---

## 🐳 Docker Support

Build and run using Docker:

```bash
docker build -t auth-backend .
docker run -p 8080:8080 auth-backend
```

---

## 📌 Future Improvements

* Email verification system
* Multi-factor authentication (MFA)
* Rate limiting & brute-force protection
* API monitoring & logging

---

## 👨‍💻 Author

**Sumit Chouhan**

* GitHub: https://github.com/sumitchouhan9826

---

## ⭐ Support

If you found this project helpful, please give it a ⭐ on GitHub!
