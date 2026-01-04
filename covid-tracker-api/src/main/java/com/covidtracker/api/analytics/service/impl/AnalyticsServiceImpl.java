package com.covidtracker.api.analytics.service.impl;

import com.covidtracker.api.analytics.model.ApiRequestMetric;
import com.covidtracker.api.analytics.repository.AnalyticsRepository;
import com.covidtracker.api.analytics.service.AnalyticsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Service implementation for analytics operations
 * Aggregates MongoDB metrics using in-memory processing
 * 
 * Approach: Fetches all metrics via findAll(), then calculates aggregations in Java
 * Suitable for small-medium datasets. For large datasets, consider MongoDB aggregation pipelines.
 */
@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsServiceImpl.class);

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsServiceImpl(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    /**
     * Counts requests per endpoint
     * Fetches all metrics, then groups by endpoint and counts
     */
    @Override
    public Map<String, Long> getEndpointStats() {
        logger.debug("Fetching endpoint statistics");
        
        List<ApiRequestMetric> allMetrics = analyticsRepository.findAll();
        Map<String, Long> endpointCounts = new HashMap<>();
        
        for (ApiRequestMetric metric : allMetrics) {
            String endpoint = metric.getEndpoint();
            endpointCounts.put(endpoint, endpointCounts.getOrDefault(endpoint, 0L) + 1);
        }
        
        return endpointCounts;
    }

    /**
     * Calculates average response time per endpoint
     * Two-step process: sum response times, then divide by count
     */
    @Override
    public Map<String, Double> getResponseTimeStats() {
        logger.debug("Fetching response time statistics");
        
        List<ApiRequestMetric> allMetrics = analyticsRepository.findAll();
        Map<String, Long> endpointTotalTime = new HashMap<>();
        Map<String, Long> endpointCounts = new HashMap<>();
        
        // Sum response times and count requests per endpoint
        for (ApiRequestMetric metric : allMetrics) {
            String endpoint = metric.getEndpoint();
            long responseTime = metric.getResponseTime();
            
            endpointTotalTime.put(endpoint, endpointTotalTime.getOrDefault(endpoint, 0L) + responseTime);
            endpointCounts.put(endpoint, endpointCounts.getOrDefault(endpoint, 0L) + 1);
        }
        
        // Calculate average: total time / count
        Map<String, Double> averageResponseTimes = new HashMap<>();
        for (String endpoint : endpointTotalTime.keySet()) {
            long totalTime = endpointTotalTime.get(endpoint);
            long count = endpointCounts.get(endpoint);
            double average = count > 0 ? (double) totalTime / count : 0.0;
            averageResponseTimes.put(endpoint, average);
        }
        
        return averageResponseTimes;
    }

    /**
     * Categorizes requests by success/error per endpoint
     * Success = 2xx, Error = 4xx/5xx status codes
     */
    @Override
    public Map<String, Map<String, Long>> getSuccessErrorRates() {
        logger.debug("Fetching success/error rate statistics");
        
        List<ApiRequestMetric> allMetrics = analyticsRepository.findAll();
        Map<String, Map<String, Long>> result = new HashMap<>();
        
        for (ApiRequestMetric metric : allMetrics) {
            String endpoint = metric.getEndpoint();
            int statusCode = metric.getStatusCode();
            
            // Initialize rates map for endpoint if needed
            Map<String, Long> rates = result.get(endpoint);
            if (rates == null) {
                rates = new HashMap<>();
                rates.put("success", 0L);
                rates.put("error", 0L);
                result.put(endpoint, rates);
            }
            
            // Categorize by status code
            if (statusCode >= 200 && statusCode < 300) {
                rates.put("success", rates.get("success") + 1);
            } else if (statusCode >= 400) {
                rates.put("error", rates.get("error") + 1);
            }
        }
        
        return result;
    }

    /**
     * Returns recent request timeline (last 100 requests)
     * Fetches all metrics, sorts by timestamp, returns formatted data
     */
    @Override
    public List<Map<String, Object>> getTimeline() {
        logger.debug("Fetching request timeline");
        
        List<ApiRequestMetric> allMetrics = analyticsRepository.findAll();
        
        // Sort by timestamp descending (most recent first)
        Collections.sort(allMetrics, new Comparator<ApiRequestMetric>() {
            @Override
            public int compare(ApiRequestMetric a, ApiRequestMetric b) {
                return b.getTimestamp().compareTo(a.getTimestamp());
            }
        });
        
        // Take last 100 requests and convert to map format
        List<Map<String, Object>> timeline = new ArrayList<>();
        int limit = Math.min(100, allMetrics.size());
        
        for (int i = 0; i < limit; i++) {
            ApiRequestMetric metric = allMetrics.get(i);
            Map<String, Object> entry = new HashMap<>();
            entry.put("timestamp", metric.getTimestamp());
            entry.put("endpoint", metric.getEndpoint());
            entry.put("method", metric.getMethod());
            entry.put("statusCode", metric.getStatusCode());
            entry.put("responseTime", metric.getResponseTime());
            timeline.add(entry);
        }
        
        return timeline;
    }
}

