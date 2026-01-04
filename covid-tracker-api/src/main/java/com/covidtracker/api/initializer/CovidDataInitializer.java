package com.covidtracker.api.initializer;

import com.covidtracker.api.model.CovidData;
import com.covidtracker.api.repository.CovidDataRepository;
import com.covidtracker.api.util.CsvParserUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initializes database with CSV data on application startup
 * Uses ApplicationRunner to ensure all Spring beans are ready before execution
 */
@Component
public class CovidDataInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(CovidDataInitializer.class);

    @Value("${covid.data.file}")
    private Resource csvResource;

    private final CovidDataRepository covidDataRepository;

    public CovidDataInitializer(CovidDataRepository covidDataRepository) {
        this.covidDataRepository = covidDataRepository;
    }

    /**
     * Runs after Spring context is fully initialized
     * Checks if database is empty and loads CSV data if needed
     */
    @Override
    public void run(ApplicationArguments args) {
        logger.info("Starting COVID-19 data initialization...");

        try {
            // Only load data if database is empty (idempotent operation)
            if (covidDataRepository.isEmpty()) {
                logger.info("Database is empty. Loading initial data from CSV...");
                loadInitialData();
            } else {
                logger.info("Database already contains data. Skipping initial load.");
            }
        } catch (Exception e) {
            // Don't crash app if initialization fails - allows app to start without data
            logger.error("Error during COVID-19 data initialization", e);
            logger.warn("Application will continue, but COVID-19 data may not be available.");
        }

        logger.info("COVID-19 data initialization completed.");
    }

    /**
     * Parses CSV file and bulk inserts data into database
     * Uses @Transactional in repository for atomicity (all-or-nothing)
     */
    private void loadInitialData() throws Exception {
        logger.info("Parsing CSV file: {}", csvResource.getFilename());
        
        // Parse CSV into CovidData objects
        List<CovidData> covidDataList = CsvParserUtil.parseCovidDataFromCsv(csvResource);
        logger.info("Parsed {} country records from CSV", covidDataList.size());

        if (covidDataList.isEmpty()) {
            logger.warn("CSV file is empty or contains no valid data.");
            return;
        }

        // Bulk insert using repository (more efficient than individual inserts)
        int rowsAffected = covidDataRepository.bulkUpdateOrInsertCovidData(covidDataList);
        logger.info("Successfully loaded {} country records into database", rowsAffected);
    }
}