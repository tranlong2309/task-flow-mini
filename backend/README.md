# Task Flow Backend

Backend service cho ứng dụng Task Flow, xây dựng theo mô hình Clean Architecture / Hexagonal Architecture sử dụng Java 17 và Spring Boot 3.

## Cấu trúc chính

- `domain`: business model, rules, ports
- `application`: use cases, service orchestration, DTOs
- `infrastructure`: persistence, adapters, web controllers, config

## Run app

```bash
mvn spring-boot:run
```

## Build

```bash
mvn clean package
```

## Tech stack

- Java 17
- Spring Boot 3.3.x
- Spring Data JPA
- MariaDB
- Maven
