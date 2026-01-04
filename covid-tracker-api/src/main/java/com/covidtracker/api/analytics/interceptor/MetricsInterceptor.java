package com.covidtracker.api.analytics.interceptor;

import com.covidtracker.api.analytics.model.ApiRequestMetric;
import com.covidtracker.api.analytics.repository.AnalyticsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * Interceptor to capture API request metrics and store them in MongoDB
 * Runs asynchronously to avoid blocking API responses
 */
@Component
public class MetricsInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MetricsInterceptor.class);
    
    private final AnalyticsRepository analyticsRepository;

    public MetricsInterceptor(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // Store start time in request attribute
        request.setAttribute("startTime", System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                                Object handler, Exception ex) {
        String requestPath = request.getRequestURI();
        
        // Only track /api/** endpoints, exclude /actuator/**
        if (!requestPath.startsWith("/api/")) {
            return;
        }

        try {
            Long startTime = (Long) request.getAttribute("startTime");
            if (startTime == null) {
                return;
            }

            long responseTime = System.currentTimeMillis() - startTime;
            String method = request.getMethod();
            int statusCode = response.getStatus();
            LocalDateTime timestamp = LocalDateTime.now();

            // Create metric object
            ApiRequestMetric metric = new ApiRequestMetric(
                requestPath,
                method,
                responseTime,
                statusCode,
                timestamp
            );

            // Save to MongoDB asynchronously (non-blocking)
            CompletableFuture.runAsync(() -> {
                try {
                    analyticsRepository.save(metric);
                    logger.debug("Saved metric: {} {} - {}ms - {}", method, requestPath, responseTime, statusCode);
                } catch (Exception e) {
                    logger.error("Error saving metric to MongoDB", e);
                }
            });

        } catch (Exception e) {
            logger.error("Error capturing metrics", e);
        }
    }
}

