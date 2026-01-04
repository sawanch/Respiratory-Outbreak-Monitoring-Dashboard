package com.covidtracker.api.util;

import com.covidtracker.api.model.CovidData;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * CSV Parser Utility - Parses Johns Hopkins COVID-19 CSV data
 * 
 * CSV Format: Province/State, Country/Region, Lat, Long, date columns...
 * - Multiple rows per country (one per province/state)
 * - Date columns contain cumulative confirmed cases
 * 
 * Processing:
 * 1. Identifies date columns (excludes metadata: Province/State, Country/Region, Lat, Long)
 * 2. Extracts latest 2 dates for calculating new cases
 * 3. Aggregates provinces/states by country (sums values)
 * 4. Estimates other metrics (deaths, recovered, etc.) since CSV only has confirmed cases
 */
public class CsvParserUtil {

    private static final Logger logger = LoggerFactory.getLogger(CsvParserUtil.class);

    /**
     * Parses CSV file and returns list of CovidData objects aggregated by country
     * 
     * @param csvResource CSV file resource
     * @return List of CovidData objects (one per country)
     * @throws IOException if file cannot be read or parsed
     */
    public static List<CovidData> parseCovidDataFromCsv(Resource csvResource) throws IOException {
        logger.info("Parsing COVID-19 data from CSV file...");
        List<CovidData> dataList = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(csvResource.getInputStream(), StandardCharsets.UTF_8));
             CSVParser csvParser = new CSVParser(reader, CSVFormat.TDF.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {

            // Step 1: Extract all column headers
            List<String> headers = csvParser.getHeaderNames();
            
            // Step 2: Identify date columns (exclude metadata columns)
            List<String> dateColumns = identifyDateColumns(headers);
            
            // Step 3: Get the two most recent dates for calculating new cases
            String latestDate = dateColumns.isEmpty() ? null : dateColumns.get(dateColumns.size() - 1);
            String previousDate = dateColumns.size() < 2 ? null : dateColumns.get(dateColumns.size() - 2);

            // Step 4: Aggregate data by country (since CSV has multiple rows per country)
            Map<String, CovidData> countryMap = aggregateDataByCountry(csvParser, latestDate, previousDate);
            
            // Step 5: Convert map values to list for return
            dataList.addAll(countryMap.values());
            
            logger.info("Successfully parsed {} country records from CSV", dataList.size());
        }

        return dataList;
    }

    /**
     * Identifies date columns by excluding metadata columns
     * 
     * @param headers All column headers from CSV
     * @return List of date column names
     */
    private static List<String> identifyDateColumns(List<String> headers) {
        List<String> dateColumns = new ArrayList<>();
        for (String header : headers) {
            if (!header.equalsIgnoreCase("Province/State") && 
                !header.equalsIgnoreCase("Country/Region") && 
                !header.equalsIgnoreCase("Lat") && 
                !header.equalsIgnoreCase("Long")) {
                dateColumns.add(header);
            }
        }
        return dateColumns;
    }

    /**
     * Aggregates CSV records by country (sums province/state values)
     * 
     * @param csvParser CSV parser with records
     * @param latestDate Latest date column name
     * @param previousDate Previous date column name (for calculating new cases)
     * @return Map of country name to CovidData object
     */
    private static Map<String, CovidData> aggregateDataByCountry(
            CSVParser csvParser, String latestDate, String previousDate) {
        
        Map<String, CovidData> countryMap = new HashMap<>();

        for (CSVRecord record : csvParser) {
            try {
                String country = record.get("Country/Region");
                
                // Extract case counts for latest and previous dates
                Long latestCases = latestDate != null ? parseLong(record.get(latestDate)) : 0L;
                Long previousCases = previousDate != null ? parseLong(record.get(previousDate)) : 0L;
                
                // Check if this country already exists (from previous province/state)
                CovidData existingData = countryMap.get(country);
                if (existingData == null) {
                    // New country - create fresh data object
                    CovidData data = createCovidData(country, latestCases, previousCases, latestDate);
                    countryMap.put(country, data);
                } else {
                    // Country exists - aggregate values from multiple provinces/states
                    aggregateExistingCountry(existingData, latestCases, previousCases);
                }
            } catch (Exception e) {
                // Skip malformed records and continue processing
                logger.warn("Skipping invalid record: {}", e.getMessage());
            }
        }
        
        return countryMap;
    }

    /**
     * Creates CovidData object with estimated metrics
     * 
     * CSV only contains confirmed cases. Estimates:
     * - Deaths: 2% of total cases
     * - Recovered: 90% of total cases
     * - Active: 8% of total cases
     * - Critical: 1% of total cases
     * 
     * @param country Country name
     * @param latestCases Latest date case count
     * @param previousCases Previous date case count
     * @param latestDate Latest date string
     * @return New CovidData object
     */
    private static CovidData createCovidData(String country, Long latestCases, 
                                             Long previousCases, String latestDate) {
        CovidData data = new CovidData();
        data.setCountry(country);
        data.setTotalCases(latestCases);
        data.setNewCases(latestCases - previousCases); // Calculate new cases
        data.setLastUpdated(latestDate != null ? latestDate : "Unknown");
        
        // Estimate other metrics based on typical ratios
        data.setTotalDeaths((long)(latestCases * 0.02));        // ~2% mortality
        data.setNewDeaths((long)((latestCases - previousCases) * 0.02));
        data.setTotalRecovered((long)(latestCases * 0.90));     // ~90% recovery
        data.setActiveCases((long)(latestCases * 0.08));        // ~8% active
        data.setCriticalCases((long)(latestCases * 0.01));      // ~1% critical
        
        return data;
    }

    /**
     * Aggregates data for an existing country (adds province/state values)
     * Recalculates estimated metrics based on new totals
     * 
     * @param existingData Existing CovidData object to update
     * @param latestCases Latest date case count from current record
     * @param previousCases Previous date case count from current record
     */
    private static void aggregateExistingCountry(CovidData existingData, 
                                                  Long latestCases, Long previousCases) {
        // Aggregate case counts
        existingData.setTotalCases(existingData.getTotalCases() + latestCases);
        existingData.setNewCases(existingData.getNewCases() + (latestCases - previousCases));
        
        // Recalculate estimated metrics based on new totals
        long totalCases = existingData.getTotalCases();
        long newCases = existingData.getNewCases();
        existingData.setTotalDeaths((long)(totalCases * 0.02));
        existingData.setNewDeaths((long)(newCases * 0.02));
        existingData.setTotalRecovered((long)(totalCases * 0.90));
        existingData.setActiveCases((long)(totalCases * 0.08));
        existingData.setCriticalCases((long)(totalCases * 0.01));
    }

    /**
     * Safely parses string to Long, handles null, empty, N/A, and comma-formatted numbers
     * 
     * @param value String value to parse
     * @return Parsed Long value, or 0L if parsing fails
     */
    private static Long parseLong(String value) {
        if (value == null || value.trim().isEmpty() || value.equalsIgnoreCase("N/A")) {
            return 0L;
        }
        try {
            // Remove commas and parse (e.g., "1,000,000" -> 1000000)
            return Long.parseLong(value.replace(",", "").trim());
        } catch (NumberFormatException e) {
            logger.debug("Failed to parse number: {}", value);
            return 0L;
        }
    }
}

