# Maersk Container Booking

Spring Boot WebFlux service for container booking and availability management.

This service provides APIs to:
- Check container availability between ports
- Create new container bookings with generated booking references

---

## 🧩 Features

- Reactive & non-blocking architecture using **Spring WebFlux (Netty)**
- **Java 21** with modern language features
- **Gradle Kotlin DSL** build
- Extensible, modular design aligned with clean architecture principles
- TDD-friendly setup with JUnit 5 and Reactor Test

---

## ⚙️ Tech Stack

| Layer | Technology |
|-------|-------------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.7 (WebFlux) |
| Build Tool | Gradle (Kotlin DSL) |
| Reactive Runtime | Reactor Netty |
| Testing | JUnit 5, Reactor Test |
| Documentation | Springdoc OpenAPI |

---

## ▶️ Run Locally

Make sure you have **Java 21** and **Gradle 8.5+** installed.

```bash
# Clone the repository
git clone https://github.com/mallikarjunelluru99-prog/maersk-container-booking.git
cd maersk-container-booking

# Run the application
./gradlew bootRun
