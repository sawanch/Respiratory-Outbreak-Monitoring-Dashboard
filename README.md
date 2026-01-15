# Respiratory Outbreak Monitoring Dashboard

A production-grade full-stack web application for real-time tracking and visualization of respiratory disease outbreak statistics across COVID-19, Flu, RSV, and Pneumonia. Implements enterprise patterns including polyglot persistence (MySQL + MongoDB), distributed caching (Redis), AI-powered insights (OpenAI GPT-4), RESTful API design, and comprehensive analytics tracking. Deployed on AWS EC2 with Nginx reverse proxy and demonstrates modern software engineering practices including layered architecture, dependency injection, and automated monitoring.

**Live Demo**: http://3.95.130.213 (AWS EC2 Production Deployment)

## Table of Contents

- [Technical Overview](#technical-overview)
- [Architecture](#architecture)
- [Key Features](#key-features)
- [Technology Stack](#technology-stack)
- [System Design](#system-design)
- [Project Structure](#project-structure)
- [Setup and Installation](#setup-and-installation)
- [API Documentation](#api-documentation)
- [Deployment Architecture](#deployment-architecture)
- [Technical Challenges and Solutions](#technical-challenges-and-solutions)
- [Performance Optimizations](#performance-optimizations)
- [Security Considerations](#security-considerations)
- [Testing Strategy](#testing-strategy)

## Technical Overview

This application processes time-series outbreak data from authoritative sources (Johns Hopkins University), performing ETL operations to transform raw CSV data into structured, queryable information. The system implements a three-tier architecture with clear separation of concerns, utilizing polyglot persistence to optimize for different data access patterns:

- **MySQL** for transactional outbreak data requiring ACID compliance
- **MongoDB** for flexible, document-based analytics and metrics storage
- **Redis** for distributed caching with configurable TTL

The backend exposes RESTful APIs following industry standards, integrates with OpenAI's GPT-4 for intelligent insights, and implements comprehensive request tracking for operational visibility. The frontend provides a responsive, mobile-first interface built with vanilla JavaScript, demonstrating proficiency without framework dependencies.

## Architecture

### High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                          CLIENT LAYER                               │
│                     (Browser / Mobile / API Clients)                │
└───────────────────────────────┬─────────────────────────────────────┘
                                │ HTTPS/HTTP
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    WEB SERVER LAYER                                 │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Nginx Reverse Proxy                                         │   │
│  │  - Static asset serving                                      │   │
│  │  - Request routing and load balancing                        │   │
│  │  - SSL/TLS termination                                       │   │
│  └──────────────────────────┬───────────────────────────────────┘   │
└───────────────────────────────┼─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│                    APPLICATION LAYER                                │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │  Spring Boot Application (Port 8080)                         │   │
│  │                                                              │   │
│  │  ┌────────────────┐  ┌────────────────┐  ┌───────────────┐ │   │
│  │  │  Outbreak      │  │  AI Insights   │  │  Analytics    │ │   │
│  │  │  Module        │  │  Module        │  │  Module       │ │   │
│  │  └────────────────┘  └────────────────┘  └───────────────┘ │   │
│  │                                                              │   │
│  │  ┌─────────────────────────────────────────────────────────┐│   │
│  │  │  Cross-Cutting Concerns                                 ││   │
│  │  │  - Exception Handling   - Logging                       ││   │
│  │  │  - Request Interceptors - CORS Configuration            ││   │
│  │  └─────────────────────────────────────────────────────────┘│   │
│  └──────────────┬──────────────────┬────────────────┬──────────┘   │
└─────────────────┼──────────────────┼────────────────┼──────────────┘
                  │                  │                │
                  ▼                  ▼                ▼
    ┌──────────────────┐  ┌──────────────┐  ┌──────────────────┐
    │  MySQL 8.0       │  │  MongoDB     │  │  Redis Cache     │
    │  (Primary Data)  │  │  (Analytics) │  │  (Optional)      │
    └──────────────────┘  └──────────────┘  └──────────────────┘
                              ▲
                              │
                  ┌───────────┴────────────┐
                  │  OpenAI GPT-4 API      │
                  │  (AI Insights)         │
                  └────────────────────────┘
```

### Backend Component Architecture

The application follows a layered architecture pattern with dependency injection:

```
┌─────────────────────────────────────────────────────────────────────┐
│  CONTROLLER LAYER (@RestController)                                 │
│  - HTTP request/response handling                                   │
│  - Input validation and sanitization                                │
│  - HTTP status code management                                      │
│  - OpenAPI/Swagger documentation                                    │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│  SERVICE LAYER (@Service)                                           │
│  - Business logic implementation                                    │
│  - Transaction management                                           │
│  - Data aggregation and transformation                              │
│  - Cache coordination                                               │
│  - External API integration (OpenAI)                                │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│  REPOSITORY LAYER (@Repository)                                     │
│  - Data access abstraction                                          │
│  - JDBC operations with prepared statements                         │
│  - MongoDB document operations                                      │
│  - Connection pool management (HikariCP)                            │
└───────────────────────────────┬─────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────┐
│  DATA LAYER                                                         │
│  - MySQL: Relational data with ACID guarantees                     │
│  - MongoDB: Document storage for analytics                          │
│  - Redis: Distributed cache for performance                         │
└─────────────────────────────────────────────────────────────────────┘
```

## Key Features

### Core Functionality

**Real-Time Data Processing**
- ETL pipeline for CSV-to-database transformation
- Province/state-level data aggregation to country totals
- Automated data initialization on application startup
- Manual refresh capability via REST endpoint

**RESTful API Design**
- Resource-oriented endpoint structure
- Proper HTTP method usage (GET, POST, PUT, DELETE)
- Standardized JSON responses
- HTTP status code compliance
- CORS support for cross-origin requests

**AI-Powered Intelligence**
- OpenAI GPT-4 integration for contextual insights
- Dynamic prompt engineering based on country data
- Safety recommendations and risk assessments
- Trend analysis and predictive warnings
- Graceful degradation when AI service is unavailable

**Comprehensive Analytics**
- Real-time API request tracking via interceptor pattern
- Time-series metrics storage in MongoDB
- Endpoint performance monitoring
- Usage pattern analysis
- AI-generated insights on API usage trends

**Performance Optimization**
- Optional Redis caching with configurable TTL
- Connection pooling with HikariCP (20 max, 5 min idle)
- Database query optimization with indexed columns
- Asynchronous operations for non-blocking I/O
- Efficient CSV parsing with Apache Commons CSV

**Production Monitoring**
- Spring Boot Actuator health endpoints
- Custom metrics exposure
- Application metadata via /actuator/info
- Log aggregation with rotation (10MB files, 10 history)
- Structured logging for operational insights

## Technology Stack

### Backend Technologies

| Category | Technology | Version | Purpose |
|----------|-----------|---------|---------|
| **Language** | Java | 11 | Type-safe, performant runtime with mature ecosystem |
| **Framework** | Spring Boot | 2.7.14 | Enterprise-grade framework with embedded Tomcat |
| **Build Tool** | Maven | 3.6+ | Dependency management and lifecycle automation |
| **Primary Database** | MySQL | 8.0 | ACID-compliant relational database |
| **Analytics Database** | MongoDB Atlas | Cloud | Schema-flexible document store for metrics |
| **Cache** | Redis | Latest | In-memory data structure store |
| **Connection Pool** | HikariCP | (via Spring Boot) | High-performance JDBC connection pooling |
| **API Documentation** | Springdoc OpenAPI | 1.7.0 | Interactive API documentation (Swagger) |
| **Monitoring** | Spring Boot Actuator | (via Spring Boot) | Production-ready monitoring and management |
| **AI Integration** | OpenAI API | GPT-4o-mini | Natural language processing and insights |
| **CSV Processing** | Apache Commons CSV | 1.10.0 | Efficient CSV parsing and validation |
| **Logging** | Logback | (via Spring Boot) | Structured logging with file rotation |
| **Testing** | JUnit 5, Mockito | (via Spring Boot) | Unit and integration testing |

### Frontend Technologies

| Technology | Purpose |
|-----------|---------|
| **HTML5** | Semantic markup for accessibility and SEO |
| **CSS3** | Modern styling with flexbox and grid |
| **JavaScript (ES6+)** | Vanilla JS demonstrating core competency |
| **Bootstrap 5** | Responsive UI framework and component library |
| **Fetch API** | Modern HTTP client for asynchronous requests |
| **HighCharts** | Data visualization and interactive charts |

### Infrastructure & DevOps

| Technology | Purpose |
|-----------|---------|
| **AWS EC2** | Cloud compute (t2.micro instance) |
| **Nginx** | Reverse proxy, load balancing, static file serving |
| **Systemd** | Service management and auto-restart |
| **Ubuntu 22.04 LTS** | Server operating system |
| **Git** | Version control and deployment automation |
| **SSH** | Secure remote server administration |

## System Design

### Design Patterns Implemented

**Layered Architecture**
- Separation of concerns across Controller, Service, Repository layers
- Dependency injection via Spring's IoC container
- Interface-based programming for loose coupling

**Repository Pattern**
- Abstracts data access logic from business logic
- Enables easier testing through mocking
- Supports multiple data sources (MySQL, MongoDB)

**Singleton Pattern**
- Service beans managed by Spring container
- Configuration classes for shared resources
- Cache managers and connection pools

**Interceptor Pattern**
- MetricsInterceptor for cross-cutting analytics tracking
- Executes before/after each HTTP request
- Non-intrusive monitoring implementation

**Factory Pattern**
- Row mappers for database result set transformation
- Error response builders for consistent error handling

**Strategy Pattern**
- Configurable caching strategy (Redis enabled/disabled)
- Multiple database backends based on data type

### Data Flow Architecture

**Request Processing Flow**:
```
Client Request
    ↓
Nginx (Port 80)
    ↓
Spring Boot (Port 8080)
    ↓
MetricsInterceptor (Request logging)
    ↓
Controller (Input validation)
    ↓
Service (Business logic)
    ↓
Cache Layer (Redis - if enabled)
    ↓ (cache miss)
Repository (Data access)
    ↓
Database (MySQL/MongoDB)
    ↓
Response transformation
    ↓
JSON response to client
```

**Analytics Pipeline**:
```
HTTP Request → Interceptor → Async Processing → MongoDB → Analytics API
```

**AI Insights Flow**:
```
User Request → Controller → Service → OpenAI API → Response Parsing → Client
```

### Database Schema Design

**MySQL Schema** (Relational - ACID compliance):
```sql
covid_data
├── id (PK, AUTO_INCREMENT)
├── country (VARCHAR, INDEXED)
├── province_state (VARCHAR)
├── confirmed_cases (BIGINT)
├── deaths (BIGINT)
├── recovered (BIGINT)
├── active_cases (BIGINT)
├── last_updated (TIMESTAMP)
└── CONSTRAINT unique_country_province
```

**MongoDB Schema** (Document - Flexible):
```javascript
api_metrics: {
  _id: ObjectId,
  endpoint: String,
  method: String,
  statusCode: Number,
  responseTime: Number,
  timestamp: ISODate,
  clientIp: String,
  userAgent: String
}
```

## Project Structure

```
Respiratory-Outbreak-Monitoring-Dashboard/
│
├── outbreak-tracker-api/                    # Backend Spring Boot Application
│   ├── src/main/java/com/outbreaktracker/api/
│   │   │
│   │   ├── outbreak/                        # Outbreak Data Module
│   │   │   ├── controller/
│   │   │   │   └── CovidDataController.java       # REST endpoints
│   │   │   ├── service/
│   │   │   │   ├── CovidDataService.java          # Business logic interface
│   │   │   │   └── impl/
│   │   │   │       └── CovidDataServiceImpl.java  # Implementation
│   │   │   ├── repository/
│   │   │   │   └── CovidDataRepository.java       # Data access layer
│   │   │   ├── model/
│   │   │   │   ├── CovidData.java                 # Domain entity
│   │   │   │   └── GlobalStats.java               # DTO
│   │   │   ├── mapper/
│   │   │   │   └── CovidDataRowMapper.java        # ResultSet mapping
│   │   │   └── initializer/
│   │   │       └── CovidDataInitializer.java      # Startup data loader
│   │   │
│   │   ├── aiinsights/                      # AI Integration Module
│   │   │   ├── controller/
│   │   │   │   └── AiInsightsController.java
│   │   │   ├── service/
│   │   │   │   ├── AiInsightsService.java
│   │   │   │   └── impl/
│   │   │   │       └── AiInsightsServiceImpl.java # OpenAI integration
│   │   │   └── model/
│   │   │       ├── CovidInsightsResponse.java
│   │   │       ├── InsightCard.java
│   │   │       └── PrecautionGroup.java
│   │   │
│   │   ├── analytics/                       # Analytics Module
│   │   │   ├── controller/
│   │   │   │   └── AnalyticsController.java
│   │   │   ├── service/
│   │   │   │   ├── AnalyticsService.java
│   │   │   │   ├── AnalyticsAiService.java
│   │   │   │   └── impl/
│   │   │   │       ├── AnalyticsServiceImpl.java
│   │   │   │       └── AnalyticsAiServiceImpl.java
│   │   │   ├── repository/
│   │   │   │   └── AnalyticsRepository.java       # MongoDB operations
│   │   │   ├── interceptor/
│   │   │   │   └── MetricsInterceptor.java        # Request tracking
│   │   │   ├── config/
│   │   │   │   └── WebMvcConfig.java              # MVC configuration
│   │   │   └── model/
│   │   │       ├── ApiRequestMetric.java
│   │   │       ├── AnalyticsInsightsResponse.java
│   │   │       └── AnalyticsInsightCard.java
│   │   │
│   │   ├── common/                          # Shared Components
│   │   │   ├── config/
│   │   │   │   ├── CorsConfig.java                # Cross-origin configuration
│   │   │   │   ├── RedisConfig.java               # Cache configuration
│   │   │   │   └── CacheConfig.java               # Cache abstraction
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java    # Centralized error handling
│   │   │   ├── model/
│   │   │   │   └── ErrorResponse.java             # Standardized error format
│   │   │   └── util/
│   │   │       └── CsvParserUtil.java             # CSV processing utility
│   │   │
│   │   └── OutbreakTrackerApiApplication.java     # Application entry point
│   │
│   ├── src/main/resources/
│   │   ├── application.properties           # Base configuration
│   │   ├── application-local.properties     # Development overrides
│   │   └── data/
│   │       └── covid19_confirmed_global.csv # Dataset
│   │
│   ├── src/test/java/                       # Test suite
│   │   └── com/outbreaktracker/api/
│   │       ├── outbreak/
│   │       │   ├── controller/
│   │       │   │   └── CovidDataControllerTest.java
│   │       │   └── service/impl/
│   │       │       └── CovidDataServiceImplTest.java
│   │
│   ├── sql/                                 # Database scripts
│   │   ├── 01_create_database.sql
│   │   └── 02_create_covid_data_table.sql
│   │
│   ├── scripts/
│   │   └── download_data.sh                 # Data update automation
│   │
│   ├── logs/
│   │   └── outbreak-tracker-api.log         # Application logs
│   │
│   ├── pom.xml                              # Maven configuration
│   └── README.md                            # Backend documentation
│
├── outbreak-tracker-ui/                     # Frontend Application
│   ├── index.html                           # Main dashboard
│   ├── analytics.html                       # Analytics page
│   ├── css/
│   │   └── styles.css                       # Custom styles
│   ├── js/
│   │   ├── app.js                           # Main application logic
│   │   ├── analytics.js                     # Analytics page logic
│   │   └── config.js                        # API configuration
│   └── README.md                            # Frontend documentation
│
└── README.md                                # Project documentation
```

## Setup and Installation

### Prerequisites

- **Java Development Kit (JDK) 11+**
- **Apache Maven 3.6+**
- **MySQL 8.0+**
- **MongoDB Atlas Account** (free tier available)
- **OpenAI API Key** (optional, for AI features)
- **Modern Web Browser**

### Local Development Setup

#### 1. Database Configuration

**MySQL Setup:**
```bash
# Create database
mysql -u root -p -e "CREATE DATABASE covid_tracker;"

# Run schema scripts
mysql -u root -p covid_tracker < outbreak-tracker-api/sql/01_create_database.sql
mysql -u root -p covid_tracker < outbreak-tracker-api/sql/02_create_covid_data_table.sql
```

**MongoDB Setup:**
1. Create free cluster at [MongoDB Atlas](https://www.mongodb.com/cloud/atlas)
2. Create database: `covid_analytics`
3. Whitelist your IP address
4. Create database user
5. Copy connection string

#### 2. Backend Configuration

Create `outbreak-tracker-api/src/main/resources/application-local.properties`:

```properties
# MySQL Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/covid_tracker?useSSL=false&serverTimezone=UTC
spring.datasource.username=your_mysql_username
spring.datasource.password=your_mysql_password

# MongoDB Configuration
spring.data.mongodb.uri=mongodb+srv://username:password@cluster.mongodb.net/covid_analytics

# OpenAI Configuration (optional)
openai.api.key=your_openai_api_key
openai.enabled=true

# CORS Configuration
cors.allowed.origins=http://localhost:8000,http://127.0.0.1:8000

# Redis Configuration
spring.cache.redis.enabled=false
```

#### 3. Build and Run Backend

```bash
cd outbreak-tracker-api

# Compile and run tests
mvn clean install

# Run application
mvn spring-boot:run

# Alternative: Run packaged JAR
mvn clean package
java -jar target/outbreak-tracker-api.jar
```

**Verify Backend:**
```bash
# Health check
curl http://localhost:8080/actuator/health

# Test API
curl http://localhost:8080/api/global

# Access Swagger documentation
open http://localhost:8080/swagger-ui/index.html
```

#### 4. Frontend Configuration

Update `outbreak-tracker-ui/js/config.js`:

```javascript
const CONFIG = {
    API_BASE_URL: 'http://localhost:8080/api',
    SWAGGER_UI_URL: 'http://localhost:8080/swagger-ui/index.html'
};
```

#### 5. Run Frontend

```bash
cd outbreak-tracker-ui

# Option 1: Python HTTP server
python3 -m http.server 8000

# Option 2: Node.js http-server
npx http-server -p 8000 -c-1 --cors

# Option 3: PHP (if installed)
php -S localhost:8000
```

Access at: http://localhost:8000

## API Documentation

### Outbreak Data Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| `GET` | `/api/global` | Aggregated worldwide statistics | `GlobalStats` |
| `GET` | `/api/countries` | List all countries | `List<CovidData>` |
| `GET` | `/api/country/{name}` | Country-specific data | `CovidData` |
| `POST` | `/api/refresh` | Trigger data refresh | `200 OK` |

### AI Insights Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| `GET` | `/api/ai-insights/country/{name}` | AI-generated safety insights | `CovidInsightsResponse` |

### Analytics Endpoints

| Method | Endpoint | Description | Response |
|--------|----------|-------------|----------|
| `GET` | `/api/analytics/summary` | API usage metrics | `AnalyticsSummary` |
| `GET` | `/api/analytics/timeline` | Time-series data | `List<TimeSeriesData>` |
| `GET` | `/api/analytics/ai-insights` | AI-analyzed metrics | `AnalyticsInsightsResponse` |

### Actuator Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/actuator/health` | Application health status |
| `GET` | `/actuator/info` | Application metadata |
| `GET` | `/actuator/metrics` | Performance metrics |

### Example API Responses

**GET /api/global**
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

**GET /api/ai-insights/country/United States**
```json
{
  "country": "United States",
  "timestamp": "2024-01-15T10:30:00Z",
  "insights": [
    {
      "title": "Current Risk Assessment",
      "content": "Based on recent data analysis...",
      "severity": "MEDIUM",
      "icon": "shield-check"
    }
  ],
  "precautionGroups": [
    {
      "groupName": "Personal Protection",
      "items": ["Wear masks in crowded spaces", "Maintain social distance"]
    }
  ],
  "summary": "Current outbreak trends indicate moderate risk..."
}
```

**Interactive Documentation**: http://localhost:8080/swagger-ui/index.html

## Deployment Architecture

### AWS EC2 Production Deployment

**Infrastructure Components:**

```
Internet
    ↓
AWS Elastic IP (Static)
    ↓
EC2 Instance (t2.micro, Ubuntu 22.04)
    ↓
Nginx (Port 80)
    ├── Static Files → /var/www/outbreak-tracker-ui
    └── API Proxy → localhost:8080
        ↓
    Spring Boot Application (Systemd Service)
        ├── MySQL (localhost:3306)
        ├── MongoDB Atlas (cloud)
        └── Redis (optional, localhost:6379)
```

**Deployment Steps:**

1. **EC2 Instance Setup**
```bash
# Launch t2.micro instance with Ubuntu 22.04
# Configure Security Groups:
#   - SSH: Port 22 (your IP only)
#   - HTTP: Port 80 (0.0.0.0/0)
#   - Custom: Port 8080 (for testing)

# Allocate and associate Elastic IP
```

2. **Server Configuration**
```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install dependencies
sudo apt install openjdk-11-jdk nginx mysql-server -y

# Configure MySQL
sudo mysql_secure_installation
mysql -u root -p < sql/01_create_database.sql
mysql -u root -p covid_tracker < sql/02_create_covid_data_table.sql
```

3. **Application Deployment**
```bash
# Build JAR locally
mvn clean package

# Transfer to EC2
scp -i your-key.pem target/outbreak-tracker-api.jar ubuntu@your-ec2-ip:~/

# Create systemd service
sudo nano /etc/systemd/system/outbreak-tracker.service
```

**Systemd Service Configuration:**
```ini
[Unit]
Description=Respiratory Outbreak Monitoring API
After=network.target mysql.service

[Service]
Type=simple
User=ubuntu
WorkingDirectory=/home/ubuntu
ExecStart=/usr/bin/java -jar /home/ubuntu/outbreak-tracker-api.jar
Restart=on-failure
RestartSec=10
StandardOutput=append:/var/log/outbreak-tracker/app.log
StandardError=append:/var/log/outbreak-tracker/error.log

[Install]
WantedBy=multi-user.target
```

4. **Nginx Configuration**
```nginx
server {
    listen 80;
    server_name your-domain.com;
    
    # Frontend
    location / {
        root /var/www/outbreak-tracker-ui;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # Backend API
    location /api/ {
        proxy_pass http://localhost:8080/api/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # Actuator endpoints (restrict access)
    location /actuator/ {
        proxy_pass http://localhost:8080/actuator/;
        allow your-monitoring-ip;
        deny all;
    }
}
```

5. **Start Services**
```bash
# Enable and start application
sudo systemctl enable outbreak-tracker
sudo systemctl start outbreak-tracker
sudo systemctl status outbreak-tracker

# Configure Nginx
sudo ln -s /etc/nginx/sites-available/outbreak-tracker /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

## Technical Challenges and Solutions

### Challenge 1: Polyglot Persistence Management

**Problem**: Managing multiple database systems (MySQL, MongoDB) with different connection patterns and transaction semantics.

**Solution**: 
- Implemented separate repository layers with appropriate abstractions
- Used Spring's transaction management for MySQL operations
- Leveraged MongoDB's native async operations for analytics
- Created unified error handling across different data sources

### Challenge 2: Performance Optimization for Large Datasets

**Problem**: Processing and serving data for 195+ countries with time-series information efficiently.

**Solution**:
- Implemented HikariCP connection pooling (max 20, min idle 5)
- Added optional Redis caching layer with 2-minute TTL
- Optimized MySQL queries with proper indexing on country column
- Used efficient CSV parsing with Apache Commons CSV
- Implemented pagination for large result sets

### Challenge 3: External API Integration Reliability

**Problem**: OpenAI API calls can fail or timeout, impacting user experience.

**Solution**:
- Implemented circuit breaker pattern concepts
- Added comprehensive error handling and fallback responses
- Used WebClient for reactive, non-blocking HTTP calls
- Configured appropriate timeouts (30 seconds)
- Provided graceful degradation when AI features unavailable

### Challenge 4: Cross-Origin Resource Sharing (CORS)

**Problem**: Frontend and backend on different domains/ports during development and production.

**Solution**:
- Implemented centralized CORS configuration
- Environment-specific allowed origins
- Proper handling of preflight requests
- Credential support for future authentication needs

### Challenge 5: Production Monitoring and Debugging

**Problem**: Need visibility into application behavior and performance in production.

**Solution**:
- Integrated Spring Boot Actuator for health checks
- Implemented request interceptor for comprehensive analytics
- Configured structured logging with rotation policies
- Created custom metrics for business KPIs
- Set up MongoDB analytics for operational insights

## Performance Optimizations

### Backend Optimizations

1. **Connection Pooling**
   - HikariCP with optimized pool sizing
   - Minimum idle connections for fast response
   - Connection timeout configuration

2. **Caching Strategy**
   - Redis integration for frequently accessed data
   - Configurable TTL based on data volatility
   - Cache invalidation on data refresh

3. **Database Query Optimization**
   - Indexed columns for frequent queries
   - Efficient aggregation queries
   - Prepared statements to prevent SQL injection

4. **Asynchronous Processing**
   - Non-blocking analytics logging
   - Reactive WebClient for external APIs
   - Thread pool configuration for concurrent requests

### Frontend Optimizations

1. **Efficient DOM Manipulation**
   - Minimal reflows and repaints
   - Event delegation for dynamic elements
   - Debouncing for search inputs

2. **Asset Optimization**
   - CDN usage for Bootstrap and libraries
   - Minified CSS and JavaScript
   - Lazy loading of analytics charts

3. **API Request Optimization**
   - Caching of static data in browser
   - Reduced unnecessary API calls
   - Efficient error handling

## Security Considerations

### Implemented Security Measures

1. **Input Validation**
   - Server-side validation of all inputs
   - SQL injection prevention via prepared statements
   - XSS protection through proper output encoding

2. **API Security**
   - CORS configuration to limit origins
   - Rate limiting considerations
   - Secure header configuration

3. **Database Security**
   - Parameterized queries
   - Principle of least privilege for DB users
   - Connection encryption (SSL/TLS)

4. **Configuration Management**
   - Sensitive data in environment variables
   - Profile-based configuration
   - API keys not committed to version control

5. **Production Deployment**
   - Actuator endpoints restricted by IP
   - Nginx reverse proxy security
   - Regular security updates

### Future Security Enhancements

- JWT-based authentication
- Role-based access control (RBAC)
- SSL/TLS certificate installation
- API key authentication for external consumers
- Rate limiting middleware

## Testing Strategy

### Unit Testing

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=CovidDataServiceImplTest

# Run with coverage report
mvn test jacoco:report
```

### Integration Testing

```bash
# Test with test profile
mvn test -Dspring.profiles.active=test
```

### Manual API Testing

```bash
# Test global statistics
curl -X GET http://localhost:8080/api/global

# Test country data
curl -X GET http://localhost:8080/api/country/India

# Test AI insights
curl -X GET http://localhost:8080/api/ai-insights/country/India

# Test analytics
curl -X GET http://localhost:8080/api/analytics/summary

# Test health endpoint
curl -X GET http://localhost:8080/actuator/health
```

### Load Testing Considerations

- JMeter configuration for stress testing
- Expected load: 100 concurrent users
- Target response time: < 500ms for cached requests
- Target response time: < 2s for database queries

## Data Source

**Johns Hopkins University CSSE COVID-19 Dataset**

- **Repository**: https://github.com/CSSEGISandData/COVID-19
- **Update Frequency**: Daily
- **Coverage**: 195+ countries and regions
- **Granularity**: Province/state level (aggregated to country in application)
- **Format**: CSV time-series data

**Data Processing Pipeline**:
1. CSV file ingestion via application startup initializer
2. Parsing with Apache Commons CSV library
3. Data validation and cleaning
4. Aggregation from province/state to country level
5. Derived metric calculation (active cases, recovery rates)
6. Persistence to MySQL with timestamp tracking

---

**Project demonstrates**: Full-stack development, microservices architecture principles, cloud deployment, API design, database design, performance optimization, security best practices, and modern software engineering methodologies.

**Author**: Sawan Chakraborty
**Production URL**: http://3.95.130.213
