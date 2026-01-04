# Skills & Learnings from COVID-19 Tracker Project

## Introduction

This document outlines the technical skills you will learn and practice by completing the COVID-19 Tracker project end-to-end. The project is a full-stack application featuring a Spring Boot REST API backend with MySQL and MongoDB databases, and a responsive frontend dashboard with analytics visualization.

By working through this project, you will gain hands-on experience with industry-standard technologies, architectural patterns, and best practices that are highly valued in software development roles.

---

## Skills from Resume Covered

The following tables map each skill from your resume to its usage in this project with specific examples:

### Java

| Skill | Example | Location |
|:------|:-------|:--------|
| **Object-oriented programming** | Classes, objects, encapsulation, getters/setters | `model/CovidData.java`<br>Lines 9–138 |
| **Spring Boot framework** | `@RestController`, `@Service`, `@Repository` annotations | `controller/CovidDataController.java`<br>Line 20 |
| **REST API development** | `@GetMapping`, `@PostMapping`, `ResponseEntity` | `controller/CovidDataController.java`<br>Lines 35–40 |

### JavaScript

| Skill | Example | Location |
|:------|:-------|:--------|
| **Async/await** | Asynchronous function calls with `await` | `js/app.js`<br>`async function fetchGlobalStats()` (lines 67–94) |
| **Event handling** | `onclick` handlers, `addEventListener` | `index.html` (line 38)<br>`js/app.js` (line 30) |
| **Data fetching** | Fetch API for HTTP requests | `js/app.js` - `fetch(\`${CONFIG.API_BASE_URL}/global\`)` (line 76) |
| **Chart rendering** | Highcharts library | `js/analytics.js` - `Highcharts.chart()` (line 72) |

### HTML5

| Skill | Example | Location |
|:------|:-------|:--------|
| **Semantic HTML** | `<nav>`, `<main>`, `<section>` instead of `<div>` | `index.html` (lines 29, 49, content sections) |
| **Form handling** | Input elements, user interaction | `index.html` (line 177) |
| **Accessibility** | Meta tags, semantic structure for screen readers | `index.html` (line 6, semantic structure) |

### CSS3

| Skill | Example | Location |
|:------|:-------|:--------|
| **Styling** | Colors, fonts, spacing, shadows | `css/styles.css` (lines 6–16, 27, 41) |
| **Responsive design** | Media queries for mobile layouts | `css/styles.css` - `@media (max-width: 768px)` (line 320) |

### Bootstrap

| Skill | Example | Location |
|:------|:-------|:--------|
| **Bootstrap 5 components** | Navbar, cards, buttons | `index.html` (lines 29, 88–145, 35) |
| **Grid system** | Responsive grid layout | `index.html` - `<div class="row g-3">` and `<div class="col-12 col-sm-6 col-lg-3">` (line 88) |

### JSON

| Skill | Example | Location |
|:------|:-------|:--------|
| **JSON parsing** | Converting JSON response to JavaScript objects | `js/app.js` - `await response.json()` (line 82) |
| **JSON serialization** | Converting Java objects to JSON | All Spring Boot controller methods |

### REST APIs

| Skill | Example | Location |
|:------|:-------|:--------|
| **HTTP methods** | GET for retrieval, POST for actions | `controller/CovidDataController.java` (lines 35, 76) |
| **Status codes** | 200 OK, 404 NOT FOUND | `controller/CovidDataController.java` - `ResponseEntity.ok()` (line 39), `ResponseEntity.status(HttpStatus.NOT_FOUND)` (line 66) |
| **RESTful conventions** | URL patterns like `/api/countries` | `controller/CovidDataController.java` (lines 21, 56) |

### MySQL

| Skill | Example | Location |
|:------|:-------|:--------|
| **SQL queries** | SELECT, INSERT, UPDATE | `repository/CovidDataRepository.java` (lines 40–44, 59–64, 85–98) |
| **Query execution** | Running queries manually to check results | `sql/01_create_database.sql` (line 4)<br>`sql/02_create_covid_data_table.sql` (lines 8–24) |
| **JDBC** | Java Database Connectivity | `repository/CovidDataRepository.java` - `jdbcTemplate.query()` (line 45) |
| **Connection pooling** | HikariCP | `application.properties` (lines 54–60) |

### MongoDB

| Skill | Example | Location |
|:------|:-------|:--------|
| **NoSQL document model** | Documents instead of tables | `analytics/model/ApiRequestMetric.java` - @Document (lines 12–22) |
| **Spring Data MongoDB** | Repository pattern | `analytics/repository/AnalyticsRepository.java` (line 15) |
| **Query derivation** | Automatic queries from method names | `analytics/repository/AnalyticsRepository.java` - `findByEndpoint()` (line 26) |

### Git

| Skill | Example | Location |
|:------|:-------|:--------|
| **.gitignore** | Excluding sensitive files | `.gitignore` - ignores `application-local.properties` (line 68) |

### Maven

| Skill | Example | Location |
|:------|:-------|:--------|
| **pom.xml configuration** | Dependencies, project structure | `pom.xml` (lines 30–87) |
| **Build lifecycle** | Compile, test, package | `mvn clean install` |

### Spring

| Skill | Example | Location |
|:------|:-------|:--------|
| **Dependency injection** | `@Autowired`, constructor injection | `controller/CovidDataController.java` (lines 28–30) |
| **Auto-configuration** | Spring Boot auto-configures components | Spring Boot sets up web server, DB connections, etc. |

### JUnit

| Skill | Example | Location |
|:------|:-------|:--------|
| **Unit testing** | Testing individual methods | `src/test/java/.../CovidDataControllerTest.java` (line 66) |
| **Mocking** | Mockito to mock dependencies | `CovidDataControllerTest.java` - `@Mock` (line 29) |
| **Assertions** | Verifying expected behavior | `assertEquals()` (line 74) |

### Shell Scripting

| Skill | Example | Location |
|:------|:-------|:--------|
| **Bash scripting** | Automation scripts, variables, conditionals | `scripts/download_covid_data.sh` (lines 1–84) |
| **Cron jobs** | Scheduled tasks | `scripts/download_covid_data.sh` (line 15) |

### Data & ETL

| Skill | Example | Location |
|:------|:-------|:--------|
| **CSV parsing** | Reading and parsing CSV files | `util/CsvParserUtil.java` - `parseCovidDataFromCsv()` (lines 44–77) |
| **Data extraction** | Extracting data from CSV columns | `util/CsvParserUtil.java` - `identifyDateColumns()` (lines 79–95) |
| **Data transformation** | Aggregating data by country, calculating metrics | `util/CsvParserUtil.java` - `aggregateDataByCountry()` (lines 97–135) |
| **Data loading** | Bulk insert/update operations | `repository/CovidDataRepository.java` - `bulkUpdateOrInsertCovidData()` (lines 85–124) |
| **ETL pipeline** | Extract, transform, load process on startup | `initializer/CovidDataInitializer.java` - `run()` (lines 39–57) |

---

## Additional Skills Gained

### Backend Development

| Skill | Description | Location |
|:------|:------------|:--------|
| **Spring Boot** | Auto-configuration, starter dependencies, embedded server | `CovidTrackerApiApplication.java` |
| **Spring Data MongoDB** | Repository pattern, query derivation, document mapping | `analytics/repository/AnalyticsRepository.java` |
| **Spring Boot Actuator** | Health checks, metrics, info endpoints | `/actuator/health`, `/actuator/metrics` |
| **JDBC Template** | Raw SQL access, RowMapper, transactions | `repository/CovidDataRepository.java` |
| **RESTful API Design** | HTTP methods, status codes, endpoints | All controller classes |
| **MVC & Separation of Concerns** | Layered architecture | Controller: `controller/CovidDataController.java`<br>Service: `service/impl/CovidDataServiceImpl.java`<br>Repository: `repository/CovidDataRepository.java` |
| **Swagger/OpenAPI** | API documentation | `/swagger-ui/index.html` |

### Frontend Development

| Skill | Description | Location |
|:------|:------------|:--------|
| **AJAX/Fetch API** | Asynchronous HTTP requests | `js/app.js`, `js/analytics.js` |
| **DOM Manipulation** | Dynamic content updates | `js/app.js`, `js/analytics.js` |
| **Highcharts** | Data visualization | `js/analytics.js` |
| **Responsive Design** | Mobile layouts | `css/styles.css`, `index.html` |
| **Configuration Management** | Environment-based config | `js/config.js` |

### Database Concepts

| Skill | Description | Location |
|:------|:------------|:--------|
| **Hybrid DB Architecture** | SQL for transactional, NoSQL for analytics | `repository/CovidDataRepository.java`, `analytics/repository/AnalyticsRepository.java` |
| **NoSQL Document Model** | Document-based storage | `analytics/model/ApiRequestMetric.java` |
| **Connection Pooling** | Efficient DB connections | `application.properties` |
| **Query Derivation** | Automatic query generation | `analytics/repository/AnalyticsRepository.java` |

### Configuration & Deployment

| Skill | Description | Location |
|:------|:------------|:--------|
| **Configuration Management** | Environment-based config | `application.properties`, `application-local.properties` |
| **Environment Variables** | Secure credentials | `application.properties` (examples) |
| **CORS Configuration** | Cross-origin policies | `config/CorsConfig.java` |
| **Security Best Practices** | Profile-based secure configs | `.gitignore`, `application-local.properties` |

### Observability & Monitoring

| Skill | Description | Location |
|:------|:------------|:--------|
| **Logging** | SLF4J, Logback, file logging | `application.properties`, `controller/CovidDataController.java`, `GlobalExceptionHandler.java` |
| **Interceptors & Metrics** | Capture API usage, timing | `analytics/interceptor/MetricsInterceptor.java` |
| **Analytics Dashboard** | Real-time metrics visualization | `analytics.html`, `js/analytics.js` |
| **Performance Monitoring** | Track response times | `MetricsInterceptor.java`, `AnalyticsServiceImpl.java` |
| **Error Tracking** | Monitor success/error rates | `AnalyticsServiceImpl.java` |

### Software Engineering Concepts

| Skill | Description | Location |
|:------|:------------|:--------|
| **Separation of Concerns** | Layered responsibilities | Controller, Service, Repository classes |
| **Code Organization** | Package structure | `analytics/` package |
| **Code Documentation** | JavaDoc, comments | All Java files |
| **Exception Handling** | Global error handling | `exception/GlobalExceptionHandler.java` |
| **API Design Patterns** | RESTful conventions | All controller classes |

### Cloud & Infrastructure

| Skill | Description | Location |
|:------|:------------|:--------|
| **MongoDB Atlas** | Cloud-hosted database, secure connections | `application-local.properties` |

---

## Summary

### Skills from Resume Covered: **16 skills**
Java, JavaScript, HTML5, CSS3, Bootstrap, JSON, REST APIs, MySQL, MongoDB, Git, Maven, Spring, JUnit, Shell Scripting, Data & ETL, Logging

### Additional Skills Gained: **35+ skills**
Backend: Spring Boot, Spring Data MongoDB, Actuator, JDBC Template, Interceptors, Swagger, MVC Design Pattern  
Frontend: AJAX/Fetch API, DOM Manipulation, Highcharts, Responsive Design, Dynamic Chart Creation  
Database: Hybrid Architecture, NoSQL, Connection Pooling, Query Derivation  
Configuration: Environment Variables, CORS, Security Best Practices  
Observability: Metrics, Analytics Dashboard, Performance Monitoring, Error Tracking  
Software Engineering: Separation of Concerns, Code Organization, Documentation, Exception Handling  
Cloud: MongoDB Atlas

### Total Skills Demonstrated: **51+ technical skills**
