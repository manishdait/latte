# Latte - Ticket Management Application

This project, is a full-stack ticket management application built to test and demonstrate my skills in Spring Boot and Angular. It provides basic ticket management functionalities, including user management, ticket creation/editing/deletion, and ticket commenting.

[![Java CI with Maven](https://github.com/manishdait/latte/actions/workflows/maven.yml/badge.svg)](https://github.com/manishdait/latte/actions/workflows/maven.yml)
[![Latest Version](https://img.shields.io/badge/version-0.0.1-blue)](README.md)

## ✨ Features
* **User Management:** User creation and deletion.
* **Ticket Management:** Ticket creation, editing, and deletion.
* **Ticket Comments:** Adding comments to tickets.
* **Simple to Use:** Designed to be easy and simple to use.

## 🐳 Installation

```yml
services:
  postgres-db:
    container_name: postgres-db
    image: postgres:alpine
    restart: always
    ports:
      - 5432:5432
    volumes:
      - pg_data:/var/lib/postgresql/date
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
      - POSTGRES_DB=latte_db
    networks:
      - latte-network
  latte-server:
    container_name: latte-server
    image: manishdait/latte-api:0.0.1
    restart: always
    depends_on:
      - postgres-db
    networks:
      - latte-network
    volumes:
      - data:/data
  latte-client:
    container_name: latte-client
    image: manishdait/latte-client:0.0.1
    restart: always
    depends_on:
      - latte-server
    ports:
      - 80:80
    networks:
      - latte-network

volumes:
  pg_data: {}
  data: {}

networks:
  latte-network:
```
Once the application is running, access it in your web browser at `http://localhost`.

**Default Credentials**

Default username: `admin@admin.com`

To retrieve the password, Open your terminaland run the following command:

```bash
  docker exec -it latte-server cat ../data/cred
```

