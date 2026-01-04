package com.covidtracker.api.analytics.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document for storing API request metrics
 * Used for observability and analytics
 */
@Document(collection = "api_metrics")
public class ApiRequestMetric {

    @Id
    private String id;
    
    private String endpoint;
    private String method;
    private long responseTime; // in milliseconds
    private int statusCode;
    private LocalDateTime timestamp;

    public ApiRequestMetric() {
    }

    public ApiRequestMetric(String endpoint, String method, long responseTime, int statusCode, LocalDateTime timestamp) {
        this.endpoint = endpoint;
        this.method = method;
        this.responseTime = responseTime;
        this.statusCode = statusCode;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public long getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(long responseTime) {
        this.responseTime = responseTime;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

