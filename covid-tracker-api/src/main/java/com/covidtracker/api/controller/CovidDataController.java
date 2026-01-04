package com.covidtracker.api.controller;

import com.covidtracker.api.model.CovidData;
import com.covidtracker.api.model.GlobalStats;
import com.covidtracker.api.service.CovidDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for COVID-19 data endpoints
 * Handles all HTTP requests for COVID-19 statistics
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
     * GET /api/global - Returns aggregated worldwide COVID-19 statistics
     */
    @GetMapping("/global")
    public ResponseEntity<GlobalStats> getGlobalStats() {
        logger.info("GET /api/global - Fetching global COVID-19 statistics");
        GlobalStats globalStats = covidDataService.getGlobalStats();
        return ResponseEntity.ok(globalStats);
    }

    /**
     * GET /api/countries - Returns COVID-19 data for all countries
     */
    @GetMapping("/countries")
    public ResponseEntity<List<CovidData>> getAllCountries() {
        logger.info("GET /api/countries - Fetching COVID-19 data for all countries");
        List<CovidData> countries = covidDataService.getAllCountriesData();
        return ResponseEntity.ok(countries);
    }

    /**
     * GET /api/country/{name} - Returns COVID-19 data for a specific country
     * Case-insensitive search (e.g., "USA", "usa", "Usa" all work)
     */
    @GetMapping("/country/{name}")
    public ResponseEntity<?> getCountryByName(@PathVariable String name) {
        logger.info("GET /api/country/{} - Fetching COVID-19 data for country: {}", name, name);
        
        CovidData countryData = covidDataService.getCountryData(name);
        
        if (countryData == null) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Country not found");
            error.put("message", "No COVID-19 data available for country: " + name);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
        }
        
        return ResponseEntity.ok(countryData);
    }

    /**
     * POST /api/refresh - Reloads COVID-19 data from CSV file
     * Use this when CSV file is updated without restarting the application
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshData() {
        logger.info("POST /api/refresh - Manually refreshing COVID-19 data");
        covidDataService.refreshCovidData();
        
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "COVID-19 data refreshed successfully");
        return ResponseEntity.ok(response);
    }

}

