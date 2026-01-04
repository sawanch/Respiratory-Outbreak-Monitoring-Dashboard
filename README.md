# COVID-19 Tracker

A full-stack web application for tracking and visualizing global COVID-19 statistics with real-time analytics and monitoring. Built with Spring Boot, MySQL, MongoDB, Redis, and JavaScript, demonstrating modern software engineering practices, API analytics tracking, and cloud deployment.

## Overview

A complete end-to-end application that processes time-series COVID-19 data. The system performs ETL operations to transform CSV data into structured database records, serves data through REST APIs, and provides comprehensive analytics and monitoring capabilities.

**Key Technologies:**
- **Backend**: Spring Boot REST API with layered architecture (Controller → Service → Repository)
- **Databases**: MySQL for transactional data, MongoDB for analytics (polyglot persistence)
- **Caching**: Redis for performance optimization (optional, configurable)
- **Analytics**: Real-time API usage tracking, request metrics, and performance monitoring
- **Monitoring**: Spring Boot Actuator for health checks, metrics, and operational insights
- **Frontend**: JavaScript with HighCharts and Bootstrap for responsive UI
- **Infrastructure**: AWS EC2 with Nginx reverse proxy

## High-Level Architecture

The system follows a three-tier architecture with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT LAYER                             │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Web Browser / Mobile App / External Services            │   │
│  │  • HTTP/REST requests                                    │   │
│  │  • JSON responses                                        │   │
│  └────────────────────┬─────────────────────────────────────┘   │
└───────────────────────┼─────────────────────────────────────────┘
                        │ HTTP/REST
                        ▼
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                           │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │  Frontend (covid-tracker-ui)                             │   │
│  │  • HTML5/CSS3/JavaScript Dashboard                       │   │
│  │  • Real-time data visualization                          │   │
│  └────────────────────┬─────────────────────────────────────┘   │
│                       │                                         │
│  ┌────────────────────▼───────────────────────────────────────┐ │
│  │  Nginx Reverse Proxy (Production)                          │ │
│  │  • Static file serving                                     │ │
│  │  • API request routing                                     │ │
│  └────────────────────┬───────────────────────────────────────┘ │
└───────────────────────┼─────────────────────────────────────────┘
                        │
                        ▼
┌────────────────────────────────────────────────────────────────┐
│                      APPLICATION LAYER                         │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │  Backend API (covid-tracker-api)                         │  │
│  │  • Spring Boot REST API                                  │  │
│  │  • Business logic and data processing                    │  │
│  │  • Request/response handling                             │  │
│  └──────┬───────────────────────────────┬───────────────────┘  │
│         │                               │                      │
│         │ Redis Cache                   │                      │
│         │ (Optional)                    │                      │
│         │                               │                      │
└─────────┼───────────────────────────────┼──────────────────────┘
          │                               │
          ▼                               ▼
┌─────────────────────┐         ┌─────────────────────┐
│   DATA LAYER        │         │   ANALYTICS LAYER   │
│                     │         │                     │
│  MySQL 8.0          │         │  MongoDB Atlas      │
│  • Primary data     │         │  • API metrics      │
│  • COVID-19 stats   │         │  • Request logs     │
│  • ACID compliance  │         │  • Usage analytics  │
└─────────────────────┘         └─────────────────────┘
```

## Low-Level Architecture

### Backend API Component Architecture

```
┌─────────────────────────────────────────────────────────────┐
│  Controller Layer (REST Endpoints)                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  CovidDataController                                  │  │
│  │  • GET /api/global                                    │  │
│  │  • GET /api/countries                                 │  │
│  │  • GET /api/country/{name}                            │  │
│  │  • POST /api/refresh                                  │  │
│  └──────────────────┬────────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼────────────────────────────────────┐  │
│  │  AnalyticsController                                  │  │
│  │  • GET /api/analytics/summary                         │  │
│  │  • GET /api/analytics/timeline                        │  │
│  └──────────────────┬────────────────────────────────────┘  │
└──────────────────────┼──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  Service Layer (Business Logic)                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  CovidDataServiceImpl                                 │  │
│  │  • Data aggregation                                   │  │
│  │  • Global stats calculation                           │  │
│  └──────────────────┬────────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼────────────────────────────────────┐  │
│  │  AnalyticsService                                     │  │
│  │  • Request tracking                                   │  │
│  │  • Metrics aggregation                                │  │
│  └──────────────────┬────────────────────────────────────┘  │
└──────────────────────┼──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│  Repository Layer (Data Access)                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │  CovidDataRepository                                  │  │
│  │  • JDBC queries                                       │  │
│  │  • Database operations                                │  │
│  └──────────────────┬────────────────────────────────────┘  │
│                     │                                       │
│  ┌──────────────────▼────────────────────────────────────┐  │
│  │  AnalyticsRepository (MongoDB)                        │  │
│  │  • Document storage                                   │  │
│  │  • Query operations                                   │  │
│  └──────────────────┬────────────────────────────────────┘  │
└──────────────────────┼──────────────────────────────────────┘
                       │
         ┌─────────────┴─────────────┐
         ▼                           ▼
┌──────────────┐          ┌──────────────────┐
│   MySQL      │          │   MongoDB Atlas  │
│   Database   │          │   (Cloud)        │
└──────────────┘          └──────────────────┘
```

### Data Flow

1. **Data Ingestion**: CSV file from Johns Hopkins → CSV Parser → MySQL Database
2. **API Request**: Client → Controller → Service → Repository → Database
3. **Caching**: Service → Redis Cache (if enabled) → Return cached data
4. **Analytics**: Request → Interceptor → MongoDB → Analytics Dashboard

### Configuration & Infrastructure

- **Connection Pooling**: HikariCP for MySQL connections
- **Logging**: Logback with file rotation and compression
- **Monitoring**: Spring Boot Actuator for health checks and metrics
- **Redis Cache**: Optional caching layer (configurable via `spring.cache.redis.enabled`)

## Components

### 1. Backend API (`covid-tracker-api`)
A Spring Boot REST API that processes COVID-19 time-series data and serves it through standardized endpoints. The API reads CSV files from Johns Hopkins University, aggregates data by country, and provides real-time statistics.

**Key Responsibilities:**
- Parse and process time-series CSV data
- Aggregate province/state data to country level
- Calculate derived metrics (active cases, recovery rates)
- Serve data via RESTful endpoints
- Track API usage and analytics

### 2. Frontend UI (`covid-tracker-ui`)
A responsive web dashboard built with vanilla JavaScript that consumes the backend API. The interface displays global statistics, country-wise data tables, and analytics visualizations.

**Key Responsibilities:**
- Display global COVID-19 statistics
- Render country data in searchable, sortable tables
- Visualize analytics metrics
- Handle user interactions and data refresh

### 3. Infrastructure
Deployed on AWS EC2 with Nginx reverse proxy, supporting automated data refresh and production-grade monitoring.

## Features

- **Global Statistics Dashboard**: Real-time worldwide COVID-19 metrics including total cases, deaths, recoveries, and active cases
- **Country-Level Analytics**: Detailed statistics for 195+ countries with search and filtering capabilities
- **API Analytics**: Monitor API usage patterns, request metrics, and endpoint performance
- **Automated Data Refresh**: Scheduled updates to ensure data currency without manual intervention
- **Cloud Deployment**: Production-ready AWS infrastructure with health monitoring and auto-scaling capabilities

## Technology Stack

### Backend
- **Java 11**: Modern Java features and performance
- **Spring Boot 2.7.14**: Enterprise application framework with embedded server
- **MySQL 8.0**: Relational database for primary data storage
- **MongoDB Atlas**: NoSQL database for analytics and metrics
- **Redis**: Optional in-memory caching for performance optimization
- **Maven**: Dependency management and build automation

### Frontend
- **HTML5/CSS3**: Semantic markup and modern styling
- **JavaScript**: Vanilla JavaScript for API integration and DOM manipulation
- **Bootstrap**: Responsive UI framework for consistent design

### Infrastructure
- **AWS EC2**: Cloud compute instances for hosting
- **Nginx**: Reverse proxy and static file serving
- **Systemd**: Service management for application lifecycle
- **Elastic IP**: Static IP address for consistent access

## Quick Start

### Prerequisites
- Java Development Kit (JDK) 11 or higher
- Maven 3.6 or higher
- MySQL 8.0 or higher
- MongoDB Atlas account (for analytics)
- Modern web browser (Chrome, Firefox, Safari, Edge)

### Local Development Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd Covid-19-Tracker
   ```

2. **Configure Backend**
   - Navigate to `covid-tracker-api/src/main/resources/`
   - Update `application.properties` with your database credentials
   - Ensure MySQL is running and accessible

3. **Start Backend API**
   ```bash
   cd covid-tracker-api
   mvn spring-boot:run
   ```
   The API will start on `http://localhost:8080`

4. **Launch Frontend**
   ```bash
   cd covid-tracker-ui
   python3 -m http.server 8000
   ```
   Open `http://localhost:8000` in your browser

## Project Structure

```
Covid-19-Tracker/
├── covid-tracker-api/          # Spring Boot REST API backend
│   ├── src/main/java/          
│   ├── src/main/resources/     
│   └── pom.xml                 
│
├── covid-tracker-ui/           # Frontend web application
│   ├── index.html              # Main dashboard page
│   ├── analytics.html          # Analytics visualization page
│   ├── css/                    
│   ├── js/                     
│   └── assets/                 
│
└── docs/                       # Additional documentation
         
```

## Documentation

- **[API Documentation](covid-tracker-api/README.md)**: Complete backend API reference
- **[UI Documentation](covid-tracker-ui/README.md)**: Frontend implementation guide
- **[AWS Deployment Guide](docs/aws-deployment-guide.md)**: Complete cloud deployment guide
- **[Learnings](docs/learnings.md)**: Technologies and concepts covered

## Data Source

The application uses the **Johns Hopkins University CSSE COVID-19 Dataset**, which provides time-series data of confirmed cases, deaths, and recoveries at the global level. The data is updated daily and includes province/state level granularity, which the application aggregates to country-level statistics.

**Why This Data Source:**
- Authoritative and widely recognized
- Regularly updated with latest statistics
- Comprehensive global coverage
- Historical time-series data available

## API Endpoints

### Example Endpoint

**Global Statistics**
```http
GET /api/global
```
Returns aggregated worldwide COVID-19 statistics.

For complete API documentation including all endpoints (`/api/countries`, `/api/country/{name}`, `/api/refresh`, `/api/analytics/*`), visit `http://localhost:8080/swagger-ui/index.html`.

## Deployment

The application is designed for cloud deployment on AWS EC2 with the following architecture:

- **Compute**: EC2 instances running Spring Boot application
- **Web Server**: Nginx reverse proxy for routing and static file serving
- **Database**: MySQL for primary data, MongoDB Atlas for analytics
- **Monitoring**: Spring Boot Actuator for health checks and metrics
- **Automation**: Cron jobs for scheduled data refresh

See [AWS Deployment Guide](docs/aws-deployment-guide.md) for detailed deployment instructions.
