package com.outbreaktracker.api.aiinsights.service.impl;

import com.outbreaktracker.api.aiinsights.model.CovidInsightsResponse;
import com.outbreaktracker.api.aiinsights.model.InsightCard;
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
import java.util.*;

/**
 * AI-powered respiratory outbreak insights using OpenAI API
 * Generates safety recommendations and trend analysis based on country outbreak data
 */
@Service
public class AiInsightsServiceImpl implements AiInsightsService {
    
    private static final Logger logger = LoggerFactory.getLogger(AiInsightsServiceImpl.class);
    
    private final CovidDataService covidDataService;
    private final WebClient webClient;
    private final String model;
    private final boolean aiEnabled;
    private final ObjectMapper objectMapper;
    
    public AiInsightsServiceImpl(
            CovidDataService covidDataService,
            @Value("${openai.api.key:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model,
            @Value("${openai.enabled:true}") boolean enabled) {
        
        this.covidDataService = covidDataService;
        this.model = model;
        this.aiEnabled = enabled && apiKey != null && !apiKey.trim().isEmpty();
        this.objectMapper = new ObjectMapper();
        
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
    
    @Override
    public CovidInsightsResponse getCountryInsights(String countryName) {
        logger.info("Generating respiratory outbreak insights for country: {}", countryName);
        
        // Fetch country data
        CovidData countryData = covidDataService.getCountryData(countryName);
        
        if (countryData == null) {
            throw new RuntimeException("Country not found: " + countryName);
        }
        
        // Build response
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
        
        // Fallback recommendations
        response.setOverallAssessment(generateFallbackAssessment(countryData));
        response.setRecommendations(generateFallbackRecommendations(countryData));
        return response;
    }
    
    /**
     * Build prompt for OpenAI API
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
                "You are a public health expert analyzing respiratory outbreak data. Provide a VARIED and NATURAL assessment.\n\n" +
                "Country: %s\n" +
                "Total Cases: %,d\n" +
                "New Cases Today: %,d (%s)\n" +
                "Total Deaths: %,d (%.1f%% mortality)\n" +
                "Active Cases: %,d (%.1f%% of total)\n\n" +
                "IMPORTANT INSTRUCTIONS:\n" +
                "- Use DIFFERENT language patterns for each country\n" +
                "- Focus on DIFFERENT aspects: some on trends, some on active cases, some on regional context\n" +
                "- DO NOT mention recovery rates or percentages unless specifically notable\n" +
                "- Vary your tone: optimistic for low numbers, cautious for moderate, urgent for high\n" +
                "- Keep assessments concise and natural (2-3 sentences)\n" +
                "- Make each response feel unique and tailored\n\n" +
                "Provide a JSON response with:\n" +
                "1. overallAssessment: A natural 2-3 sentence analysis focusing on:\n" +
                "   - Current situation (use varied descriptors: stable/concerning/improving/challenging)\n" +
                "   - Key metric that stands out (deaths, active cases, OR new cases - pick ONE)\n" +
                "   - Context-appropriate advice or observation\n\n" +
                "2. recommendations: An array of 2-3 diverse insight cards:\n" +
                "   - Mix different topics: prevention, trends, testing, vaccination, community spread\n" +
                "   - Use varied language for similar concepts\n" +
                "   - Avoid repetitive patterns across recommendations\n\n" +
                "Format each recommendation:\n" +
                "{\n" +
                "  \"icon\": \"single emoji (vary these: 😷🏥💉📊⚠️✅🔍📈📉)\",\n" +
                "  \"title\": \"Unique title (max 6 words, avoid repetition)\",\n" +
                "  \"description\": \"Specific, actionable advice (1-2 sentences)\",\n" +
                "  \"severity\": \"info|warning|success\" (based on active case rate: <5%%=success, 5-15%%=info, >15%%=warning)\n" +
                "}\n\n" +
                "Return ONLY valid JSON:\n" +
                "{\n" +
                "  \"overallAssessment\": \"your unique assessment\",\n" +
                "  \"recommendations\": [\n" +
                "    {\"icon\": \"😷\", \"title\": \"...\", \"description\": \"...\", \"severity\": \"...\"}\n" +
                "  ]\n" +
                "}",
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
     * Call OpenAI API
     */
    private String callOpenAI(String prompt, double temperature) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("temperature", temperature);
        requestBody.put("max_tokens", 600);
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a public health expert providing respiratory outbreak insights. Always respond with valid JSON.");
        
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
     * Parse AI response and populate CovidInsightsResponse
     */
    private void parseInsightsResponse(String aiResponse, CovidInsightsResponse response) {
        try {
            // Clean response - remove markdown code fences if present
            String cleanJson = aiResponse;
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.substring(7);
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.substring(3);
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.substring(0, cleanJson.length() - 3);
            }
            cleanJson = cleanJson.trim();
            
            JsonNode rootNode = objectMapper.readTree(cleanJson);
            
            // Parse overall assessment
            if (rootNode.has("overallAssessment")) {
                response.setOverallAssessment(rootNode.get("overallAssessment").asText());
            }
            
            // Parse recommendations
            if (rootNode.has("recommendations")) {
                List<InsightCard> cards = new ArrayList<>();
                JsonNode recsNode = rootNode.get("recommendations");
                
                for (JsonNode recNode : recsNode) {
                    InsightCard card = new InsightCard();
                    card.setIcon(recNode.get("icon").asText());
                    card.setTitle(recNode.get("title").asText());
                    card.setDescription(recNode.get("description").asText());
                    card.setSeverity(recNode.get("severity").asText());
                    cards.add(card);
                }
                
                response.setRecommendations(cards);
            }
        } catch (Exception e) {
            logger.error("Failed to parse AI response: {}", e.getMessage());
            throw new RuntimeException("Failed to parse AI insights", e);
        }
    }
    
    /**
     * Generate fallback assessment when AI is unavailable
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
    
    /**
     * Generate fallback recommendations when AI is unavailable
     */
    private List<InsightCard> generateFallbackRecommendations(CovidData data) {
        List<InsightCard> cards = new ArrayList<>();
        
        // Safety precautions card
        cards.add(new InsightCard(
                "😷",
                "Safety Precautions",
                "Wear masks in crowded indoor spaces, practice good hand hygiene, and maintain social distancing when possible. Stay up to date with vaccinations and boosters.",
                "info"
        ));
        
        // Trend analysis card
        Long newCases = data.getNewCases();
        if (newCases != null && newCases > 1000) {
            cards.add(new InsightCard(
                    "📈",
                    "Rising Cases",
                    String.format("New cases reported: %,d. Consider reducing non-essential activities and avoiding large gatherings to minimize exposure risk.", newCases),
                    "warning"
            ));
        } else {
            cards.add(new InsightCard(
                    "📊",
                    "Current Trend",
                    "Cases appear stable. Continue following recommended health protocols and monitor local updates for any changes in the situation.",
                    "info"
            ));
        }
        
        // Health advisory card
        cards.add(new InsightCard(
                "🏥",
                "Health Advisory",
                "Seek medical attention if you experience symptoms. Get tested if exposed or symptomatic. Vulnerable populations should take extra precautions.",
                "info"
        ));
        
        return cards;
    }
}
