package com.outbreaktracker.api.outbreak.controller;

import com.outbreaktracker.api.outbreak.model.CovidData;
import com.outbreaktracker.api.outbreak.model.GlobalStats;
import com.outbreaktracker.api.outbreak.service.CovidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for respiratory outbreak data endpoints
 * Handles all HTTP requests for outbreak statistics
 */
@RestController
@RequestMapping("/api")
public class CovidDataController {

    private static final Logger logger = LoggerFactory.getLogger(CovidDataController.class);

    private final CovidDataService covidDataService;

    public CovidDataController(CovidDataService covidDataService) {
        this.covidDataService = covidDataService;
    }

    /**
     * GET /api/global - Returns aggregated worldwide outbreak statistics
     */
    @GetMapping("/global")
    public ResponseEntity<GlobalStats> getGlobalStats() {
        logger.info("GET /api/global - Fetching global outbreak statistics");
        GlobalStats globalStats = covidDataService.getGlobalStats();
        return ResponseEntity.ok(globalStats);
    }

    /**
     * GET /api/countries - Returns outbreak data for all countries
     */
    @GetMapping("/countries")
    public ResponseEntity<List<CovidData>> getAllCountries() {
        logger.info("GET /api/countries - Fetching outbreak data for all countries");
        List<CovidData> countries = covidDataService.getAllCountriesData();
        return ResponseEntity.ok(countries);
    }

    /**
     * GET /api/country/{name} - Returns respiratory outbreak data for a specific country
     * Case-insensitive search (e.g., "USA", "usa", "Usa" all work)
     */
    @GetMapping("/country/{name}")
    public ResponseEntity<?> getCountryByName(@PathVariable String name) {
        logger.info("GET /api/country/{} - Fetching respiratory outbreak data for country: {}", name, name);
        
        CovidData countryData = covidDataService.getCountryData(name);
        
        if (countryData == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Country not found");
            error.put("message", "No respiratory outbreak data available for country: " + name);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        
        return ResponseEntity.ok(countryData);
    }

    /**
     * POST /api/refresh - Reloads respiratory outbreak data from CSV file
     * Use this when CSV file is updated without restarting the application
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshData() {
        logger.info("POST /api/refresh - Manually refreshing respiratory outbreak data");
        covidDataService.refreshCovidData();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Respiratory outbreak data refreshed successfully");
        return ResponseEntity.ok(response);
    }

}

