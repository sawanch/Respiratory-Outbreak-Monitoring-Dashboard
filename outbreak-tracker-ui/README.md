# Respiratory Outbreak Monitoring Dashboard UI

A responsive web dashboard for visualizing respiratory disease outbreak statistics across multiple diseases including COVID-19, Flu, RSV, and Pneumonia. Built with vanilla JavaScript, this frontend application consumes data from the backend API to provide real-time statistics and country-level analytics.

## Overview

A client-side web application that displays outbreak statistics with early-warning trends and country-level insights. It communicates with the backend REST API to fetch data and presents it.

## Architecture

The frontend follows a client-side architecture pattern:

```
┌─────────────────────────────────────────────────────┐
│  User Interface (HTML)                              │
│  • Dashboard layout and structure                   │
│  • Semantic HTML5 markup                            │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  Presentation Layer (CSS)                           │
│  • Responsive styling with Bootstrap 5              │
│  • Custom styles for branding and UX                │
└──────────────────┬──────────────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────────────┐
│  Application Logic (JavaScript)                     │
│  • API integration using Fetch API                  │
│  • DOM manipulation and event handling              │
│  • Data processing and formatting                   │
└──────────────────┬──────────────────────────────────┘
                   │ HTTP/REST
                   ▼
┌─────────────────────────────────────────────────────┐
│  Backend API (outbreak-tracker-api)                 │
│  • RESTful endpoints for data retrieval             │
└─────────────────────────────────────────────────────┘
```

## Key Features

- **Global Statistics Dashboard**: Displays worldwide outbreak metrics including total cases, deaths, recoveries, and active cases
- **Country Data Table**: Interactive table showing statistics for 195+ countries with:
  - Real-time search functionality
  - Sortable columns (click headers to sort)
  - Responsive design for mobile devices
- **AI-Powered Insights**: Get AI-driven safety recommendations and trend analysis for specific countries
- **Analytics Dashboard**: Separate page for API usage metrics and request analytics
- **Responsive Design**: Optimized for desktop, tablet, and mobile devices

## Technology Stack

| Component | Technology | Purpose |
|-----------|-----------|---------|
| **Markup** | HTML5 | Semantic page structure |
| **Styling** | CSS3 + Bootstrap | Responsive UI framework |
| **Scripting** | JavaScript | Application logic and API integration |

## Prerequisites

- **Backend API Running**: The backend API must be running and accessible

## Quick Start

### 1. Ensure Backend API is Running

The frontend requires the backend API to be running. Start it first:

```bash
cd ../outbreak-tracker-api
mvn spring-boot:run
```

The API should be accessible at `http://localhost:8080`

### 2. Configure API URL (if needed)

Edit `js/config.js` to update the API base URL:

```javascript
const CONFIG = {
    API_BASE_URL: 'http://localhost:8080/api',
    SWAGGER_UI_URL: 'http://localhost:8080/swagger-ui/index.html'
};
```

### 3. Launch the Frontend

```bash
cd outbreak-tracker-ui
npx http-server -p 8000 -cors
```
Open `http://localhost:8000` in your browser

## Project Structure

```
outbreak-tracker-ui/
├── index.html              # Main dashboard page
├── analytics.html          # Analytics page
├── css/
│   └── styles.css
├── js/
│   ├── app.js              # Main application logic for dashboard
│   ├── analytics.js        # Analytics page logic and chart rendering
│   └── config.js           # API configuration and endpoints
├── assets/
│   └── images/
└── README.md
```

## Features Explained

### Global Statistics Display
The dashboard displays four key metrics at the top:
- **Total Cases**: Cumulative confirmed cases worldwide
- **Total Deaths**: Cumulative deaths
- **Total Recovered**: Cumulative recoveries
- **Active Cases**: Currently active cases

### Country Data Table
- **Search**: Type in the search box to filter countries in real-time
- **Sorting**: Click any column header to sort ascending/descending
- **Responsive**: Table adapts to screen size with horizontal scrolling on mobile

### AI-Powered Insights
- Enter a country name to receive AI-generated:
  - Safety recommendations
  - Trend analysis
  - Risk assessments
  - Prevention measures

### Analytics Dashboard
A separate page (`analytics.html`) to visualize:
- API request metrics
- Endpoint usage statistics
- Request patterns and trends
- Uses Highcharts for visualization

## API Integration

The frontend communicates with the backend using the Fetch API:

### Example Endpoint

**Global Statistics**
```javascript
GET /api/global
```
Fetches aggregated worldwide statistics.

For complete API documentation including all endpoints (`/api/countries`, `/api/country/{name}`, `/api/refresh`, `/api/ai-insights/country/{name}`), visit `http://localhost:8080/swagger-ui/index.html`.

### Error Handling

The application handles various error scenarios:
- **Network Errors**: Displays user-friendly message when API is unreachable
- **API Errors**: Shows appropriate error messages for failed requests
- **Empty Data**: Handles cases when no data is available

## Deployment

The frontend is deployed on AWS EC2 with Nginx serving static files and acting as a reverse proxy for API requests. Nginx configuration routes `/api/*` requests to the backend Spring Boot application running on port 8080.
