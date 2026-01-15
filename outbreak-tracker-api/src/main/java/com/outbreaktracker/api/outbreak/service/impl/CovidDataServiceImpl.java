package com.outbreaktracker.api.outbreak.service.impl;

import com.outbreaktracker.api.outbreak.model.CovidData;
import com.outbreaktracker.api.outbreak.model.GlobalStats;
import com.outbreaktracker.api.outbreak.repository.CovidDataRepository;
import com.outbreaktracker.api.outbreak.service.CovidDataService;
import com.outbreaktracker.api.common.util.CsvParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Service implementation containing business logic for respiratory outbreak data operations
 * Acts as a middle layer between controller and repository
 */
@Service
public class CovidDataServiceImpl implements CovidDataService {

    private static final Logger logger = LoggerFactory.getLogger(CovidDataServiceImpl.class);

    private final CovidDataRepository covidDataRepository;

    @Value("${outbreak.data.file}")
    private Resource csvResource;

    public CovidDataServiceImpl(CovidDataRepository covidDataRepository) {
        this.covidDataRepository = covidDataRepository;
    }

    /**
     * Aggregates outbreak statistics from all countries to produce worldwide totals
     * Results are cached in Redis for 2 minutes
     * 
     * @return GlobalStats with aggregated worldwide data
     */
    @Override
    @Cacheable(value = "globalStats", key = "'global'")
    public GlobalStats getGlobalStats() {
        logger.debug("Fetching global outbreak statistics from database");

        List<CovidData> allData = covidDataRepository.findAllCountryData();

        long totalCases = 0;
        long totalDeaths = 0;
        long totalRecovered = 0;
        long activeCases = 0;
        long criticalCases = 0;

        for (CovidData data : allData) {
            totalCases += (data.getTotalCases() != null ? data.getTotalCases() : 0);
            totalDeaths += (data.getTotalDeaths() != null ? data.getTotalDeaths() : 0);
            totalRecovered += (data.getTotalRecovered() != null ? data.getTotalRecovered() : 0);
            activeCases += (data.getActiveCases() != null ? data.getActiveCases() : 0);
            criticalCases += (data.getCriticalCases() != null ? data.getCriticalCases() : 0);
        }

        String timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern("MMMM d, yyyy, h:mm a 'UTC'"));

        GlobalStats globalStats = new GlobalStats(
                totalCases,
                totalDeaths,
                totalRecovered,
                activeCases,
                criticalCases,
                allData.size(),
                timestamp
        );

        logger.debug("Global stats: {} total cases across {} countries", totalCases, allData.size());
        return globalStats;
    }

    /**
     * Returns respiratory outbreak data for all countries
     * Results are cached in Redis for 2 minutes
     */
    @Override
    @Cacheable(value = "countries", key = "'all'")
    public List<CovidData> getAllCountriesData() {
        logger.debug("Fetching respiratory outbreak data for all countries from database");
        return covidDataRepository.findAllCountryData();
    }

    /**
     * Retrieves respiratory outbreak data for a specific country (case-insensitive search)
     * Results are cached in Redis for 2 minutes
     * 
     * @param countryName The name of the country to search for
     * @return CovidData object if found, null otherwise
     */
    @Override
    @Cacheable(value = "country", key = "#countryName.toLowerCase()")
    public CovidData getCountryData(String countryName) {
        logger.debug("Fetching respiratory outbreak data for country: {} from database", countryName);
        return covidDataRepository.findByCountryName(countryName);
    }

    /**
     * Reloads respiratory outbreak data from CSV file and updates database
     * Clears all Redis cache entries to ensure fresh data
     * Uses @Transactional in repository for atomicity
     */
    @Override
    @CacheEvict(value = {"globalStats", "countries", "country"}, allEntries = true)
    public void refreshCovidData() {
        logger.info("Manually refreshing respiratory outbreak data from CSV file");
        
        try {
            List<CovidData> covidDataList = CsvParserUtil.parseCovidDataFromCsv(csvResource);
            logger.info("Parsed {} country records from CSV", covidDataList.size());
            
            if (covidDataList.isEmpty()) {
                logger.warn("CSV file is empty or contains no valid data.");
                return;
            }
            
            int rowsAffected = covidDataRepository.bulkUpdateOrInsertCovidData(covidDataList);
            logger.info("Respiratory outbreak data refresh completed successfully. {} rows affected", rowsAffected);
            
        } catch (Exception e) {
            logger.error("Error refreshing respiratory outbreak data from CSV file", e);
            throw new RuntimeException("Failed to refresh respiratory outbreak data: " + e.getMessage(), e);
        }
    }
}

