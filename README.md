# 🚀 HelpDesk SaaS Project

![Badge Status](https://img.shields.io/badge/status-em%20desenvolvimento-yellow)
![Badge Java](https://img.shields.io/badge/Java-21-blue.svg)
![Badge Spring](https://img.shields.io/badge/Spring%20Boot-3.x-green.svg)
![Badge Angular](https://img.shields.io/badge/Angular-18-red.svg)

A complete HelpDesk (Software as a Service) system for call, customer and technical management. This project is divided into a backend (API REST) built with Spring Boot and a frontend (SPA) built with Angular.

---

## 📋 Index

* [About the project](#about-the=project)
* [Functionalities](#functionalities)
* [Technologies Used](#tecnologies-used)
* [How to Start](#how-to-start)
    * [Prerequisites](#prerequisites)
    * [Backend (Spring Boot)](#backend-spring-boot)
    * [Frontend (Angular)](#frontend-angular)
* [ API endpoints (examples)](#api-endpoint-examples)
* [Author](#author)

---

## 📖 About the project

This project was developed as a way to expand my portfolio and document my evolution as a developer with the goal of creating a robust SaaS solution for technical support management. It implements JWT-based authentication, different access levels (Admin, Technical, Client) and a complete CRUD system for calls.

---

## ✨ Functionalities

* **Authentication & Authorization:**
    * 🔐 Login with JWT token system (Spring Security).
    * 👤 User profile management (ADMIN, TECNICO, CLIENT).
* **Call Management:**
    * 🎫 Creation, reading, updating and deletion (CRUD) of calls.
    * 🔄 Assignment of calls to technicians.
    * 📊 Filtering of calls by status (OPEN, IN PROGRESS, CLOSED), priority and client.
    * 💬 History and comments on calls.
* **Entity Management:**
    * 👥 CRUD complete for customers.
    * 🛠️ CRUD complete for technicians.
* **Dashboard (Planned):**
    * 📈 Visualization of statistics and attendance metrics.

---

## 🛠️ Technologies Used

  The project is divided into 2 repositories
  
### Backend (API)

***Java 21:** Latest version of Java LTS.
***Spring Boot 3:** Main framework for REST API construction.
***Spring Security:** For authentication and authorization (JWT).
***Spring Data JPA / Hibernate:** For data persistence and ORM.
***[Your Database, ex: PostgreSQL, MySQL, H2]:** Relational database.
***Maven / Gradle:** Dependency manager.
***Lombok:** To reduce boilerplate (repetitive code).
***Spring Validation:** For validation of input data (DTOs).

### Frontend (Client)

***Angular 18:** Main framework for Single Page Application (SPA).
***TypeScript:** JavaScript Superset that adds static typing.
***Angular Material:** UI component library.
***Angular Router:** For route management.
***Axios / Angular HttpClient:** To consume the REST API of the backend.

---



