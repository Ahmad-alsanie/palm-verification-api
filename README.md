# Palm Verification API

Palm Verification API is a Spring Boot-based application designed to provide robust and efficient palm verification services. The application leverages modern Java frameworks and best practices to ensure scalability, reliability, and maintainability.

---

## Features

- **Palm Verification**: Core functionality for handling palm recognition and verification.
- **Modular Design**: Well-structured packages for controllers, services, models, and repositories.
- **Spring Boot**: Powered by Spring Boot for rapid development and deployment.
- **Logging**: Integrated logging system for debugging and monitoring.
- **API-first Design**: Designed for seamless API integration with external systems.

---

## Available Endpoints

| Method | Endpoint                 | Description                              | Request Body | Response         |
|--------|--------------------------|------------------------------------------|--------------|------------------|
| GET    | `/api/palms`             | Retrieve all palm records               | N/A          | List of palms    |
| GET    | `/api/palms/{id}`        | Retrieve palm by ID                     | N/A          | Palm details     |
| POST   | `/api/palms`             | Add a new palm record                   | JSON Palm     | Created palm     |
| PUT    | `/api/palms/{id}`        | Update an existing palm record          | JSON Palm     | Updated palm     |
| DELETE | `/api/palms/{id}`        | Delete a palm record by ID              | N/A          | Deletion status  |
| POST   | `/api/palms/verify`      | Verify palm data                        | Palm details | Verification status |

---

## Getting Started

### Prerequisites

- **Java**: JDK 17 or later
- **Maven**: Version 3.8 or later
- **Database**: Compatible relational database (e.g., MySQL, PostgreSQL)
- **Spring Boot**: Version 3.4.0

---

### Installation

1. Clone the repository:
```bash
   git clone https://github.com/Ahmad-alsanie/palm-verification-api.git
   cd palm-verification-api
   ```

2. Build the project using Maven:
```bash
mvn clean install
```

3. Run the application:
```bash
mvn spring-boot:run
```

### Project Structure
```shell
palm-verification-api/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.palm/
│   │   │       ├── controller/       # REST Controllers
│   │   │       ├── model/            # Data Models
│   │   │       ├── repository/       # Database Repositories
│   │   │       ├── service/          # Business Logic
│   │   │       └── PalmVerificationApiApplication.java # Main Application
│   │   └── resources/
│   │       ├── application.properties # Configuration
│   │       └── static/               # Static resources (if any)
│   └── test/
│       └── ...                       # Unit and Integration Tests
│
├── pom.xml                           # Maven Configuration
├── HELP.md                           # Additional Documentation
└── README.md                         # Project Documentation
```

### Configuration
Set up the database connection in src/main/resources/application.properties:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/palm_db
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

