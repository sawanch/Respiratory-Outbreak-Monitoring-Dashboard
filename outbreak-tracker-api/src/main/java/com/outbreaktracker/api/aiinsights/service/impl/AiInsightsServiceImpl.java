package com.outbreaktracker.api.aiinsights.service.impl;

import com.outbreaktracker.api.aiinsights.model.CovidInsightsResponse;
import com.outbreaktracker.api.aiinsights.model.PrecautionGroup;
import com.outbreaktracker.api.aiinsights.service.AiInsightsService;
import com.outbreaktracker.api.outbreak.model.CovidData;
import com.outbreaktracker.api.outbreak.service.CovidDataService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for AI insights operations
 * Integrates with OpenAI API to generate intelligent outbreak analysis
 * 
 * Approach: Fetches outbreak data, builds AI prompts, and generates contextual assessments
 * Falls back to rule-based assessment if AI is unavailable
 */
@Service
public class AiInsightsServiceImpl implements AiInsightsService {

    private static final Logger logger = LoggerFactory.getLogger(AiInsightsServiceImpl.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final CovidDataService covidDataService;
    private final WebClient webClient;
    private final String model;
    private final boolean aiEnabled;

    public AiInsightsServiceImpl(CovidDataService covidDataService,
                                 @Value("${openai.api.key:}") String apiKey,
                                 @Value("${openai.model:gpt-4o-mini}") String model,
                                 @Value("${openai.enabled:true}") boolean enabled) {
        this.covidDataService = covidDataService;
        this.model = model;
        this.aiEnabled = enabled && apiKey != null && !apiKey.trim().isEmpty();

        if (this.aiEnabled) {
            this.webClient = WebClient.builder()
                    .baseUrl("https://api.openai.com/v1")
                    .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                    .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build();
            logger.info("AI Insights service initialized with model: {}", model);
        } else {
            this.webClient = null;
            logger.warn("AI Insights service running in fallback mode (OpenAI disabled or API key not set)");
        }
    }
    
    /**
     * Generates AI-powered insights for a specific country
     * Fetches country data, calls OpenAI API, and generates contextual assessment
     */
    @Override
    public CovidInsightsResponse getCountryInsights(String countryName) {
        logger.debug("Generating respiratory outbreak insights for country: {}", countryName);

        CovidData countryData = covidDataService.getCountryData(countryName);
        if (countryData == null) {
            throw new RuntimeException("Country not found: " + countryName);
        }

        CovidInsightsResponse response = new CovidInsightsResponse();
        response.setCountry(countryData.getCountry());
        response.setTotalCases(countryData.getTotalCases());
        response.setNewCases(countryData.getNewCases());
        response.setTotalDeaths(countryData.getTotalDeaths());
        response.setTotalRecovered(countryData.getTotalRecovered());
        response.setActiveCases(countryData.getActiveCases());
        response.setGeneratedAt(LocalDateTime.now().format(
                DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a")));

        if (aiEnabled) {
            try {
                String prompt = buildInsightsPrompt(countryData);
                String aiResponse = callOpenAI(prompt, 0.7);
                parseInsightsResponse(aiResponse, response);
                return response;
            } catch (Exception e) {
                logger.warn("AI request failed, using fallback: {}", e.getMessage());
            }
        }

        response.setOverallAssessment(generateFallbackAssessment(countryData));
        return response;
    }
    
    /**
     * Builds AI prompt with outbreak data and instructions
     * Creates structured prompt for OpenAI to generate insights
     */
    private String buildInsightsPrompt(CovidData data) {
        // Calculate mortality rate
        double mortalityRate = 0.0;
        if (data.getTotalCases() != null && data.getTotalCases() > 0 && data.getTotalDeaths() != null) {
            mortalityRate = (data.getTotalDeaths() * 100.0) / data.getTotalCases();
        }
        
        // Determine trend based on new cases
        String trendContext = "";
        if (data.getNewCases() != null) {
            if (data.getNewCases() > 10000) {
                trendContext = "significant daily increase";
            } else if (data.getNewCases() > 1000) {
                trendContext = "moderate daily increase";
            } else if (data.getNewCases() > 100) {
                trendContext = "minor daily increase";
            } else if (data.getNewCases() > 0) {
                trendContext = "minimal daily increase";
            } else {
                trendContext = "no new cases reported";
            }
        }
        
        // Calculate active case percentage
        double activeCaseRate = 0.0;
        if (data.getTotalCases() != null && data.getTotalCases() > 0 && data.getActiveCases() != null) {
            activeCaseRate = (data.getActiveCases() * 100.0) / data.getTotalCases();
        }
        
        return String.format(
                "You are a public health expert analyzing respiratory outbreak data.\n\n" +
                "Country: %s\n" +
                "Total Cases: %,d\n" +
                "New Cases Today: %,d (%s)\n" +
                "Total Deaths: %,d (%.1f%% mortality)\n" +
                "Active Cases: %,d (%.1f%% of total)\n\n" +
                "Provide TWO sections:\n\n" +
                "1. ASSESSMENT (EXACTLY 2 sentences):\n" +
                "   - Reference SPECIFIC numbers from the data\n" +
                "   - Analyze current situation focusing on key metrics\n" +
                "   - Keep it concise, natural and data-driven\n" +
                "   - IMPORTANT: Must be EXACTLY 2 sentences, no more\n\n" +
                "2. TARGETED_PRECAUTIONS (JSON array):\n" +
                "   Generate EXACTLY 3 demographic-specific precaution groups.\n" +
                "   Focus on: Athletes/Sports Personnel, Elderly (65+), Students/Schools.\n" +
                "   DO NOT include Healthcare Workers.\n" +
                "   Each group should have:\n" +
                "   - 'group': emoji + demographic name (e.g., '🏃 Athletes & Sports Personnel')\n" +
                "   - 'tips': array of EXACTLY 2 concise, actionable bullet points\n\n" +
                "Format:\n" +
                "ASSESSMENT:\n" +
                "Your EXACTLY 2 sentence analysis here.\n\n" +
                "TARGETED_PRECAUTIONS:\n" +
                "[\n" +
                "  {\n" +
                "    \"group\": \"🏃 Athletes & Sports Personnel\",\n" +
                "    \"tips\": [\n" +
                "      \"Limit indoor training to small groups with proper ventilation\",\n" +
                "      \"Require testing before competitions and team events\"\n" +
                "    ]\n" +
                "  }\n" +
                "]\n\n" +
                "IMPORTANT: \n" +
                "- Generate EXACTLY 3 groups (Athletes, Elderly, Students) - NO Healthcare Workers\n" +
                "- Each group must have EXACTLY 2 tips, no more, no less\n" +
                "- Make tips specific, actionable, and relevant to the current case levels",
                data.getCountry(),
                data.getTotalCases() != null ? data.getTotalCases() : 0,
                data.getNewCases() != null ? data.getNewCases() : 0,
                trendContext,
                data.getTotalDeaths() != null ? data.getTotalDeaths() : 0,
                mortalityRate,
                data.getActiveCases() != null ? data.getActiveCases() : 0,
                activeCaseRate
        );
    }
    
    /**
     * Calls OpenAI API with prompt and temperature settings
     * Returns raw AI-generated response text
     */
    private String callOpenAI(String prompt, double temperature) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", 800);
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a public health expert providing respiratory outbreak insights. Provide clear, data-driven assessments in plain text.");
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        
        requestBody.put("messages", new Object[]{systemMessage, userMessage});
        
        try {
            String response = webClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.get("choices")
                    .get(0)
                    .get("message")
                    .get("content")
                    .asText()
                    .trim();
        } catch (Exception e) {
            throw new RuntimeException("OpenAI API call failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Parses AI response and populates insights response object
     * Extracts assessment text and targeted precautions JSON
     */
    private void parseInsightsResponse(String aiResponse, CovidInsightsResponse response) {
        try {
            // Split response into assessment and precautions sections
            String assessment = "";
            List<PrecautionGroup> precautions = new ArrayList<>();
            
            // Look for ASSESSMENT: and TARGETED_PRECAUTIONS: markers
            if (aiResponse.contains("ASSESSMENT:") && aiResponse.contains("TARGETED_PRECAUTIONS:")) {
                String[] parts = aiResponse.split("TARGETED_PRECAUTIONS:");
                
                // Extract assessment
                String assessmentPart = parts[0].replace("ASSESSMENT:", "").trim();
                assessment = assessmentPart;
                
                // Extract and parse precautions JSON
                if (parts.length > 1) {
                    String precautionsJson = parts[1].trim();
                    
                    // Clean up markdown code fences if present
                    if (precautionsJson.startsWith("```json")) {
                        precautionsJson = precautionsJson.substring(7);
                    } else if (precautionsJson.startsWith("```")) {
                        precautionsJson = precautionsJson.substring(3);
                    }
                    if (precautionsJson.endsWith("```")) {
                        precautionsJson = precautionsJson.substring(0, precautionsJson.length() - 3);
                    }
                    precautionsJson = precautionsJson.trim();
                    
                    // Parse JSON array
                    JsonNode precautionsArray = objectMapper.readTree(precautionsJson);
                    for (JsonNode precautionNode : precautionsArray) {
                        PrecautionGroup group = new PrecautionGroup();
                        group.setGroup(precautionNode.get("group").asText());
                        
                        List<String> tips = new ArrayList<>();
                        JsonNode tipsNode = precautionNode.get("tips");
                        for (JsonNode tipNode : tipsNode) {
                            tips.add(tipNode.asText());
                        }
                        group.setTips(tips);
                        precautions.add(group);
                    }
                }
            } else {
                // Fallback: use entire response as assessment
                assessment = aiResponse.trim();
            }
            
            response.setOverallAssessment(assessment);
            response.setTargetedPrecautions(precautions);
            
        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", e.getMessage());
            // Set fallback assessment
            response.setOverallAssessment(aiResponse.trim());
            response.setTargetedPrecautions(new ArrayList<>());
        }
    }
    
    /**
     * Generates rule-based assessment when AI is unavailable
     * Uses simple heuristics based on case counts
     */
    private String generateFallbackAssessment(CovidData data) {
        String countryName = data.getCountry();
        long totalCases = data.getTotalCases() != null ? data.getTotalCases() : 0;
        long activeCases = data.getActiveCases() != null ? data.getActiveCases() : 0;
        
        if (totalCases > 10000000) {
            return String.format("%s has experienced significant respiratory outbreak impact with over %,d total cases. " +
                    "With %,d active cases currently, continued vigilance and health precautions are important. " +
                    "Stay informed about local guidelines and vaccination availability.",
                    countryName, totalCases, activeCases);
        } else if (totalCases > 1000000) {
            return String.format("%s has reported %,d total respiratory outbreak cases with %,d currently active. " +
                    "Following public health guidelines and staying vaccinated can help protect yourself and your community.",
                    countryName, totalCases, activeCases);
        } else {
            return String.format("%s has recorded %,d total respiratory outbreak cases. " +
                    "Continue to follow local health guidelines and maintain recommended safety measures.",
                    countryName, totalCases);
        }
    }
}
