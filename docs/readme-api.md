# COVID-19 Tracker API

A production-ready RESTful API backend built with Spring Boot that processes and serves COVID-19 statistics from Johns Hopkins University's time-series dataset. The API aggregates province/state level data to country-level statistics and provides real-time access through standardized REST endpoints.

## Overview

The COVID-19 Tracker API is a Spring Boot application that transforms raw CSV data into structured, queryable information. It processes time-series COVID-19 data, performs country-level aggregation, calculates derived metrics, and serves the data through RESTful endpoints.

**Why This API Exists:**
- **Data Processing**: Converts unstructured CSV files into structured JSON responses
- **Aggregation**: Combines province/state data into country-level statistics
- **Real-Time Access**: Provides instant access to current COVID-19 statistics via HTTP
- **Analytics**: Tracks API usage patterns and request metrics for operational insights

## Architecture

The API follows a layered architecture pattern for maintainability and scalability:

```
┌─────────────────────────────────────────────────────┐
│  Controller Layer (REST Endpoints)                  │
│  • Handles HTTP requests/responses                  │
│  • Returns appropriate HTTP status codes            │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  Service Layer (Business Logic)                     │
│  • Aggregates data from multiple sources            │
│  • Performs calculations and transformations        │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  Repository Layer (Data Access)                     │
│  • Reads and parses CSV files                       │
│  • Provides data retrieval methods                  │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  Data Source (CSV Files)                            │
│  • Johns Hopkins COVID-19 time-series data          │
│  • Updated daily with latest statistics             │
└─────────────────────────────────────────────────────┘
```

## Key Features

- **RESTful API Design**: Standard HTTP methods and status codes for predictable interactions
- **Time-Series Data Processing**: Parses CSV files with daily cumulative case data
- **Country-Level Aggregation**: Combines province/state data into unified country statistics
- **Redis Caching**: Optional Redis caching with 2-minute TTL for improved performance
- **Health Monitoring**: Spring Boot Actuator endpoints for production monitoring
- **Analytics Tracking**: MongoDB integration for API usage metrics

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Language** | Java 11 | Application runtime |
| **Framework** | Spring Boot 2.7.14 | Enterprise application framework |
| **Build Tool** | Maven 3.6+ | Dependency management and packaging |
| **Database** | MySQL 8.0 | Primary data storage |
| **Analytics DB** | MongoDB Atlas | API metrics and analytics |
| **Cache** | Redis | Optional caching layer for performance |
| **Monitoring** | Spring Boot Actuator | Health checks and metrics |
| **API Documentation** | Swagger/OpenAPI | Interactive API documentation |

## Prerequisites

Before running the application, ensure you have:

- **Java Development Kit (JDK) 11 or higher**
  ```bash
  java -version
  ```

- **Apache Maven 3.6 or higher**
  ```bash
  mvn -version
  ```

- **MySQL 8.0 or higher** (for primary data storage)
- **MongoDB Atlas account** (for analytics - cloud-hosted, no local installation needed)

## Quick Start

### 1. Configure Database

Update `src/main/resources/application.properties` with your database credentials:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/covid_tracker
spring.datasource.username=your_username
spring.datasource.password=your_password

# MongoDB Configuration
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/covid_analytics
```

### 2. Run the Application

**Option A: Using Maven**
```bash
cd covid-tracker-api
mvn spring-boot:run
```

**Option B: Using IDE**
- Open the project in IntelliJ IDEA or Eclipse
- Run `CovidTrackerApiApplication.java` as a Java application

**Option C: Using JAR**
```bash
mvn clean package
java -jar target/covid-tracker-api.jar
```

The API will start on `http://localhost:8080`

### 3. Verify Installation

```bash
# Test health endpoint
curl http://localhost:8080/actuator/health

# Test global statistics
curl http://localhost:8080/api/global
```

## API Endpoints

### Global Statistics
```http
GET /api/global
```
Returns aggregated worldwide COVID-19 statistics by summing data from all countries.

**Response:**
```json
{
  "totalCases": 676514131,
  "totalDeaths": 13530282,
  "totalRecovered": 656087267,
  "activeCases": 6896582,
  "criticalCases": 338257,
  "affectedCountries": 195,
  "lastUpdated": "2023-03-09"
}
```

For complete API documentation including all endpoints (`/api/countries`, `/api/country/{name}`, `/api/refresh`, `/actuator/health`), visit `http://localhost:8080/swagger-ui/index.html`.

## ETL and Data Processing

The API performs ETL (Extract, Transform, Load) operations on time-series CSV data from Johns Hopkins University:

1. **Extract**: Reads CSV file with daily cumulative confirmed cases from Johns Hopkins repository
2. **Transform**: 
   - Identifies latest and previous date columns
   - Groups province/state rows by country
   - Aggregates values (sums cases per country)
   - Calculates new cases (latest - previous)
3. **Load**: Stores processed data in MySQL database for fast querying

## Configuration

Key configuration properties in `application.properties`:

```properties
# Server Configuration
server.port=8080

# CSV Data File Location
covid.data.file=classpath:data/covid19_confirmed_global.csv

# Logging Configuration
logging.file.name=logs/covid-tracker-api.log
logging.file.max-size=10MB
logging.file.max-history=10

# Actuator Endpoints
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

## Project Structure

```
covid-tracker-api/
├── src/main/java/com/covidtracker/api/
│   ├── controller/
│   │   └── CovidDataController.java          # REST endpoint handlers
│   ├── service/
│   │   ├── CovidDataService.java              # Service interface
│   │   └── impl/
│   │       └── CovidDataServiceImpl.java      # Business logic implementation
│   ├── repository/
│   │   └── CovidDataRepository.java           # Data access layer
│   ├── model/
│   │   ├── CovidData.java                     
│   │   └── GlobalStats.java                   
│   ├── config/
│   │   ├── CorsConfig.java                    
│   │   └── RedisConfig.java                   
│   ├── exception/
│   │   └── GlobalExceptionHandler.java        # Error handling
│   ├── analytics/                             # Analytics module - can be coverted to microservices if needed
│   │   ├── controller/
│   │   │   └── AnalyticsController.java       
│   │   └── service/
│   │       └── AnalyticsService.java           
│   ├── mapper/
│   │   └── CovidDataRowMapper.java            
│   ├── util/
│   │   └── CsvParserUtil.java                 
│   └── CovidTrackerApiApplication.java         # Main application class
│
├── src/main/resources/
│   ├── application.properties                  # Application configuration
│   ├── application-local.properties            # Local profile overrides
│   └── data/
│       └── covid19_confirmed_global.csv         # CSV data files
│
├── src/test/java/com/covidtracker/api/
│   ├── controller/
│   │   └── CovidDataControllerTest.java
│   └── service/impl/
│       └── CovidDataServiceImplTest.java
│
└── pom.xml
```

## Logging

The application uses Logback for logging with file rotation:

- **Console Logging**: Real-time logs during development
- **File Logging**: Persistent logs in `logs/covid-tracker-api.log`
- **Rotation**: Automatic log rotation when file reaches 10MB
- **History**: Maintains up to 10 archived log files (compressed as `.gz`)

**Logging Configuration in `application.properties`:**
```properties
logging.level.root=INFO
logging.level.com.covidtracker.api=INFO
logging.file.name=logs/covid-tracker-api.log
logging.logback.rollingpolicy.max-file-size=10MB
logging.logback.rollingpolicy.max-history=10
```

**Sample Log Lines:**
```
2024-01-15 10:30:45 [main] INFO  com.covidtracker.api.CovidTrackerApiApplication - Starting CovidTrackerApiApplication
2024-01-15 10:30:47 [main] INFO  com.covidtracker.api.controller.CovidDataController - GET /api/global - Fetching global COVID-19 statistics
2024-01-15 10:30:48 [main] DEBUG com.covidtracker.api.service.impl.CovidDataServiceImpl - Fetching global COVID-19 statistics from database
```

**Log File Structure:**
- `covid-tracker-api.log` - Current log file
- `covid-tracker-api.log.2024-01-14.0.gz` - Archived compressed logs

## Testing

### Manual Testing

```bash
# Test global statistics
curl http://localhost:8080/api/global
```

You can also use **Postman** or any HTTP client to test the API endpoints. Interactive API documentation is available at `http://localhost:8080/swagger-ui/index.html`.

### Automated Testing

Run unit tests:
```bash
mvn test
```

## Deployment

The API is designed for deployment on AWS EC2 or any Java-compatible server:

1. Build the JAR file: `mvn clean package`
2. Upload JAR to server
3. Configure database connections
4. Run with: `java -jar covid-tracker-api.jar`
5. Use systemd for service management

See the main project [AWS Deployment Guide](aws-deployment-guide.md) for detailed instructions.

## Monitoring

The API includes Spring Boot Actuator for production monitoring:

- **Health Checks**: `/actuator/health` for load balancer health checks
- **Metrics**: `/actuator/metrics` for performance monitoring
- **Info**: `/actuator/info` for application metadata