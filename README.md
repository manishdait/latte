# Latte - Ticket Management Application

This project, is a full-stack ticket management application built to test and demonstrate my skills in Spring Boot and Angular. It provides basic ticket management functionalities, including user management, ticket creation/editing/deletion, and ticket commenting.

[![Java CI with Maven](https://github.com/manishdait/latte/actions/workflows/maven.yml/badge.svg)](https://github.com/manishdait/latte/actions/workflows/maven.yml)
[![Latest Version](https://img.shields.io/github/v/tag/manishdait/latte?label=Version)](README.md)

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
      - pg_data:/var/lib/postgresql/data
    environment:
      - POSTGRES_USER=postgres
      - POSTGRES_PASSWORD=password
      - POSTGRES_DB=latte_db
    networks:
      - latte-network
  latte-api:
    container_name: latte-api
    image: manishdait/latte-api
    restart: always
    depends_on:
      - postgres-db
    networks:
      - latte-network
    volumes:
      - data:/app/data
  latte-client:
    container_name: latte-client
    image: manishdait/latte-client
    restart: always
    depends_on:
      - latte-api
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
  docker exec -it latte-api cat data/.cred
```

## 🚀 Live Demo

You can explore a live demo instance of the application using the following credentials:

**Demo Credentials:**

* **Username:** `admin@admin.com`
* **Password:** `password`

<br>

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Visit%20Now-green?style=for-the-badge)](https://latte-gamma.vercel.app)
