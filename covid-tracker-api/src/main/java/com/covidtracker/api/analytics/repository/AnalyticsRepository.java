package com.covidtracker.api.analytics.repository;

import com.covidtracker.api.analytics.model.ApiRequestMetric;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB repository for API request metrics
 * Provides query methods for analytics
 * 
 * Note: Spring Data MongoDB automatically implements these methods using query derivation.
 * Method names are parsed to generate MongoDB queries (e.g., findByEndpoint -> { endpoint: ? })
 * 
 * For MySQL developers: Similar to how you write SQL queries, but here method names
 * automatically generate MongoDB queries. No need to write query strings manually.
 */
@Repository
public interface AnalyticsRepository extends MongoRepository<ApiRequestMetric, String> {

    /**
     * Get all API request metrics from MongoDB
     * Spring generates: db.api_metrics.find({})
     * 
     * Currently used by: AnalyticsServiceImpl (all 3 service methods use this)
     */
    @Override
    List<ApiRequestMetric> findAll();

    /**
     * Find all metrics for a specific endpoint
     * Spring generates: db.api_metrics.find({ endpoint: ? })
     * 
     * Example usage (currently unused, but available for future features):
     * - Filter analytics by specific endpoint
     * - Get endpoint-specific timeline
     * - Performance optimization (query only one endpoint's data)
     * 
     * For MySQL developers: Equivalent to "SELECT * FROM api_metrics WHERE endpoint = ?"
     */
    List<ApiRequestMetric> findByEndpoint(String endpoint);

    /**
     * Count total requests for a specific endpoint
     * Spring generates: db.api_metrics.count({ endpoint: ? })
     * 
     * Example usage (currently unused, but available for future features):
     * - Quick endpoint request count without loading all data
     * - Performance optimization for single-endpoint queries
     * 
     * For MySQL developers: Equivalent to "SELECT COUNT(*) FROM api_metrics WHERE endpoint = ?"
     */
    long countByEndpoint(String endpoint);

    /**
     * Find metrics within a time range
     * Spring generates: db.api_metrics.find({ timestamp: { $gte: start, $lte: end } })
     * 
     * Example usage (currently unused, but available for future features):
     * - Time-based filtering (e.g., "last 24 hours")
     * - Date range analytics
     * - Performance optimization (query only recent data instead of all data)
     * 
     * For MySQL developers: Equivalent to "SELECT * FROM api_metrics WHERE timestamp BETWEEN ? AND ?"
     */
    List<ApiRequestMetric> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
}

