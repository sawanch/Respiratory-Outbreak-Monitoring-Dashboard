package com.outbreaktracker.api.analytics.service.impl;

import com.outbreaktracker.api.analytics.model.AnalyticsInsightsResponse;
import com.outbreaktracker.api.analytics.model.AnalyticsInsightCard;
import com.outbreaktracker.api.analytics.service.AnalyticsService;
import com.outbreaktracker.api.analytics.service.AnalyticsAiService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * AI-powered analytics insights service implementation
 * Integrates with OpenAI API to generate intelligent system performance analysis
 * Similar architecture to AiInsightsServiceImpl but focused on system metrics
 */
@Service
public class AnalyticsAiServiceImpl implements AnalyticsAiService {

    private static final Logger logger = LoggerFactory.getLogger(AnalyticsAiServiceImpl.class);

    private final AnalyticsService analyticsService;
    private final WebClient webClient;
    private final String model;
    private final boolean aiEnabled;
    private final ObjectMapper objectMapper;

    public AnalyticsAiServiceImpl(AnalyticsService analyticsService,
                                  @Value("${openai.api.key:}") String apiKey,
                                  @Value("${openai.model:gpt-4o-mini}") String model,
                                  @Value("${openai.enabled:true}") boolean enabled) {
        this.analyticsService = analyticsService;
        this.model = model;
        this.aiEnabled = enabled && apiKey != null && !apiKey.trim().isEmpty();
        this.objectMapper = new ObjectMapper();

        if (this.aiEnabled) {
            this.webClient = WebClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            logger.info("Analytics AI service initialized with model: {}", model);
        } else {
            this.webClient = null;
            logger.warn("Analytics AI running in fallback mode (OpenAI disabled or API key not set)");
        }
    }
    
    @Override
    public AnalyticsInsightsResponse getSystemInsights() {
        logger.debug("Generating AI insights for system performance");

        // Fetch analytics data using individual service methods
        Map<String, Long> endpointStats = analyticsService.getEndpointStats();
        Map<String, Double> responseTimes = analyticsService.getResponseTimeStats();
        Map<String, Map<String, Long>> successError = analyticsService.getSuccessErrorRates();
        
        AnalyticsInsightsResponse response = new AnalyticsInsightsResponse();
        
        // Calculate total requests
        int totalRequests = endpointStats.values().stream().mapToInt(Long::intValue).sum();
        response.setTotalRequests(totalRequests);
        
        // Calculate metrics
        String slowestEndpoint = findSlowestEndpoint(responseTimes);
        Double maxResponseTime = responseTimes.values().stream().max(Double::compareTo).orElse(0.0);
        Double avgResponseTime = responseTimes.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        
        response.setSlowestEndpoint(slowestEndpoint);
        response.setAvgResponseTime(avgResponseTime);
        
        // Calculate error rate
        double errorRate = calculateOverallErrorRate(successError);
        response.setErrorRate(errorRate);

        if (aiEnabled) {
            try {
                // Call OpenAI API
                String prompt = buildAnalyticsPrompt(response.getTotalRequests(), 
                                                     slowestEndpoint, 
                                                     maxResponseTime, 
                                                     avgResponseTime, 
                                                     errorRate);
                String aiResponse = callOpenAI(prompt);
                parseAiResponse(aiResponse, response);
                
                response.setModel(model);
                response.setTemperature(0.7);
            } catch (Exception e) {
                logger.error("Error calling OpenAI for analytics insights", e);
                generateFallbackInsights(response);
            }
        } else {
            generateFallbackInsights(response);
        }
        
        response.setTimestamp(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        return response;
    }
    
    /**
     * Builds AI prompt for system performance analysis
     */
    private String buildAnalyticsPrompt(int totalRequests, String slowestEndpoint, 
                                       double maxResponseTime, double avgResponseTime, 
                                       double errorRate) {
        String performanceLevel;
        if (avgResponseTime < 100) {
            performanceLevel = "excellent";
        } else if (avgResponseTime < 500) {
            performanceLevel = "good";
        } else if (avgResponseTime < 2000) {
            performanceLevel = "moderate";
        } else {
            performanceLevel = "poor";
        }
        
        return String.format(
                "You are a DevOps expert analyzing API performance metrics. Provide a PROFESSIONAL and ENCOURAGING assessment.\n\n" +
                "System Metrics:\n" +
                "Total API Requests: %,d\n" +
                "Average Response Time: %.1f ms (%s performance)\n" +
                "Slowest Endpoint: %s (%.1f ms)\n" +
                "Error Rate: %.2f%%\n\n" +
                "IMPORTANT INSTRUCTIONS:\n" +
                "- Use PROFESSIONAL and ENCOURAGING language\n" +
                "- Avoid negative words like: unacceptable, terrible, bad, poor, critical, failing\n" +
                "- Use constructive phrases: \"opportunity to optimize\", \"can be improved\", \"recommended to address\", \"focus on enhancing\"\n" +
                "- Focus on DIFFERENT aspects: performance, reliability, bottlenecks, or optimization\n" +
                "- Maintain a supportive tone that motivates action\n" +
                "- Keep assessment concise (2-3 sentences)\n" +
                "- Make each response feel unique and tailored\n\n" +
                "Provide a JSON response with:\n" +
                "1. overallAssessment: A natural 2-3 sentence analysis focusing on:\n" +
                "   - Overall system health (use: healthy/stable/needs attention/requires focus)\n" +
                "   - Key performance indicator that stands out\n" +
                "   - Actionable observation with encouraging tone\n\n" +
                "2. recommendations: EXACTLY 2 recommendations in this specific order:\n" +
                "   \n" +
                "   FIRST RECOMMENDATION - \"Latency Issues\":\n" +
                "   - Title must be exactly: \"Latency Issues\"\n" +
                "   - Provide specific, encouraging advice about investigating and optimizing the slowest endpoint: %s\n" +
                "   - Use professional, constructive language (avoid \"unacceptable\")\n" +
                "   - Suggest investigating potential improvements (database queries, API calls, caching)\n" +
                "   - Use emoji: ⚡\n" +
                "   \n" +
                "   SECOND RECOMMENDATION - \"Error Monitoring\":\n" +
                "   - Title must be exactly: \"Error Monitoring\"\n" +
                "   - Provide encouraging advice about implementing error tracking and logging\n" +
                "   - Mention the current error rate: %.2f%%\n" +
                "   - Use professional, supportive language\n" +
                "   - Use emoji: 🔍\n\n" +
                "Format each recommendation:\n" +
                "{\n" +
                "  \"icon\": \"⚡ or 🔍\",\n" +
                "  \"title\": \"Latency Issues\" or \"Error Monitoring\" (exact titles required)\",\n" +
                "  \"description\": \"Specific, encouraging advice (1-2 sentences)\",\n" +
                "  \"severity\": \"info|warning|success\" (error rate: <1%%=success, 1-5%%=info, >5%%=warning)\n" +
                "}\n\n" +
                "Return ONLY valid JSON:\n" +
                "{\n" +
                "  \"overallAssessment\": \"your assessment\",\n" +
                "  \"recommendations\": [\n" +
                "    {\"icon\": \"⚡\", \"title\": \"Latency Issues\", \"description\": \"...\", \"severity\": \"...\"},\n" +
                "    {\"icon\": \"🔍\", \"title\": \"Error Monitoring\", \"description\": \"...\", \"severity\": \"...\"}\n" +
                "  ]\n" +
                "}",
                totalRequests,
                avgResponseTime,
                performanceLevel,
                slowestEndpoint,
                maxResponseTime,
                errorRate,
                slowestEndpoint,
                errorRate
        );
    }
    
    /**
     * Calls OpenAI API with the constructed prompt
     */
    private String callOpenAI(String prompt) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", 0.7);
        
        List<Map<String, String>> messages = new ArrayList<>();
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);
        messages.add(message);
        requestBody.put("messages", messages);

        String response = webClient.post()
                .uri("/chat/completions")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();

        JsonNode jsonResponse = objectMapper.readTree(response);
        String content = jsonResponse.get("choices").get(0).get("message").get("content").asText();
        
        logger.debug("Received AI response for analytics insights");
        return content;
    }
    
    /**
     * Parses AI response and populates the response object
     */
    private void parseAiResponse(String aiResponse, AnalyticsInsightsResponse response) {
        try {
            // Clean the response (remove markdown code blocks if present)
            String cleanedResponse = aiResponse.trim();
            if (cleanedResponse.startsWith("```")) {
                cleanedResponse = cleanedResponse.replaceFirst("```json\\n?", "").replaceFirst("```\\n?$", "");
            }
            
            JsonNode root = objectMapper.readTree(cleanedResponse);
            
            // Parse overall assessment
            if (root.has("overallAssessment")) {
                response.setOverallAssessment(root.get("overallAssessment").asText());
            }
            
            // Parse recommendations
            if (root.has("recommendations")) {
                List<AnalyticsInsightCard> cards = new ArrayList<>();
                for (JsonNode recNode : root.get("recommendations")) {
                    AnalyticsInsightCard card = new AnalyticsInsightCard();
                    card.setIcon(recNode.get("icon").asText());
                    card.setTitle(recNode.get("title").asText());
                    card.setDescription(recNode.get("description").asText());
                    card.setSeverity(recNode.get("severity").asText());
                    cards.add(card);
                }
                response.setRecommendations(cards);
            }
            
        } catch (Exception e) {
            logger.error("Error parsing AI response", e);
            generateFallbackInsights(response);
        }
    }
    
    /**
     * Generates rule-based recommendations when AI is unavailable
     */
    private void generateFallbackInsights(AnalyticsInsightsResponse response) {
        response.setOverallAssessment("System metrics are being actively monitored. " +
                "Focus on optimizing response times and maintaining error rates to ensure excellent performance and reliability.");
        
        List<AnalyticsInsightCard> cards = new ArrayList<>();
        
        // Always provide exactly 2 recommendations
        
        // 1. Latency Issues
        String latencyDescription;
        String latencySeverity;
        if (response.getAvgResponseTime() != null && response.getAvgResponseTime() > 1000) {
            latencyDescription = String.format("Focus on optimizing the %s endpoint, as its response time presents an opportunity for improvement. " +
                    "Investigate potential database queries or external API calls that could be enhanced to reduce response time.",
                    response.getSlowestEndpoint());
            latencySeverity = "warning";
        } else {
            latencyDescription = String.format("Monitor the %s endpoint to maintain optimal response times and proactively prevent future bottlenecks.",
                    response.getSlowestEndpoint());
            latencySeverity = "info";
        }
        cards.add(new AnalyticsInsightCard("⚡", "Latency Issues", latencyDescription, latencySeverity));
        
        // 2. Error Monitoring
        String errorDescription;
        String errorSeverity;
        if (response.getErrorRate() != null && response.getErrorRate() > 5) {
            errorDescription = String.format("Consider implementing comprehensive error tracking and logging systems to better understand the %.2f%% error rate. " +
                    "This will help identify and resolve issues proactively, ensuring a more stable API performance.",
                    response.getErrorRate());
            errorSeverity = "warning";
        } else if (response.getErrorRate() != null && response.getErrorRate() > 1) {
            errorDescription = String.format("Enhance error monitoring capabilities to maintain the current %.2f%% error rate and detect potential issues early.",
                    response.getErrorRate());
            errorSeverity = "info";
        } else {
            errorDescription = "Continue monitoring error patterns to maintain excellent system reliability and quickly identify any anomalies.";
            errorSeverity = "success";
        }
        cards.add(new AnalyticsInsightCard("🔍", "Error Monitoring", errorDescription, errorSeverity));
        
        response.setRecommendations(cards);
    }
    
    /**
     * Finds the slowest endpoint from response time stats
     */
    private String findSlowestEndpoint(Map<String, Double> responseTimes) {
        return responseTimes.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("/unknown");
    }
    
    /**
     * Calculates overall system error rate
     */
    private double calculateOverallErrorRate(Map<String, Map<String, Long>> successError) {
        long totalSuccess = 0;
        long totalError = 0;
        
        for (Map<String, Long> rates : successError.values()) {
            totalSuccess += rates.getOrDefault("success", 0L);
            totalError += rates.getOrDefault("error", 0L);
        }
        
        long total = totalSuccess + totalError;
        return total > 0 ? (totalError * 100.0) / total : 0.0;
    }
}
