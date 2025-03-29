# Software Engineering Project (2025S) – 4-Layer Web Application

## Overview

This project was developed as part of the "Software Engineering Project" lecture. It demonstrates the implementation of a four-layer web application architecture using Java, Spring Boot, Angular, TypeScript, and SQL.  
This submission represents the individual phase of the course; the group project will follow as a separate stage.

## Architecture

The application is structured into four layers:
- **Frontend:** Built with Angular and TypeScript, providing an interactive user interface.
- **REST Layer:** Defines and exposes RESTful APIs for communication between frontend and backend.
- **Service Layer:** Implements business logic using Java and Spring Boot.
- **Persistence Layer:** Manages data storage with SQL.

## Getting Started

### Prerequisites

- **Java:** OpenJDK 21
- **Node.js:** Version 22.14.0
- **npm:** Version 10.9.2
- **Git:** Version 2.x
- **Maven:** Version 3.x

### Backend

Run the Spring Boot application:
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
The backend should now be running on http://localhost:8080.

### Frontend

Open a new terminal and run:
```bash
cd frontend
npm install
npm run start
```
The frontend should now be available at  http://localhost:4200.
