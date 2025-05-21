<p align="center">
  <a>
    <img src="latte-client/public/asset/banner.svg" alt="Latte - Ticket Management Application" width="120" height="178">
  </a>

  <h1 align="center">Latte - Ticket Management Application</h1>
  
  <p align="center">
    A free and open-source personal ticket management application designed to streamline internal staff and ticket management.
  </p>
</p>

<p align="center">
  <a href="https://github.com/manishdait/latte/actions/workflows/maven.yml">
    <img src="https://github.com/manishdait/latte/actions/workflows/maven.yml/badge.svg" alt="Java CI with Maven">
  </a>
  <a href="LICENSE">
    <img src="https://img.shields.io/badge/License-MIT-yellow" alt="LICENSE">
  </a>
  <a href="README.md">
    <img src="https://img.shields.io/github/v/tag/manishdait/latte?label=Version" alt="README.md">
  </a>
</p>

## ✨ Features
Latte offers a robust set of features to simplify your internal ticket management:
* **User Management:** Effortlessly create and delete user accounts.
* **Ticket Management:** Full control over tickets, including creation, editing, and deletion.
* **Role Management:** Role creation based on different authoriitesDefine custom roles with varying levels of authority.
* **Ticket Comments:** Adding comments to tickets.Facilitate communication by adding comments to tickets.
* **Simple to Use:** Designed to be easy and simple to use.Designed for ease of use, ensuring a smooth experience for all users.

## 🐳 Installation

Getting Latte up and running with Docker. Simply use the provided `docker-compose.yml` configuration:

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
    environment:
      - CLIENT_URL=http://latte-client:80
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
Once your containers are running, you can access Latte in your web browser at  `http://localhost`.

### Default Credentials

For your initial login, use the following:

* **Default username:** `admin@admin.com`

To retrieve the default password, open your terminal and run this command:

```bash
  docker exec latte-api cat data/.cred
```

## 🚀 Live Demo

You can explore a live instance of Latte in action before installing

<p align="center">
  <a href="https://latte-gamma.vercel.app">
    <img src="https://img.shields.io/badge/Live%20Demo-Visit%20Now-green?style=for-the-badge" alt="Live Demo">
  </a>
</p>

**Demo Credentials:**

* **Username:** `admin@admin.com`
* **Password:** `password`

