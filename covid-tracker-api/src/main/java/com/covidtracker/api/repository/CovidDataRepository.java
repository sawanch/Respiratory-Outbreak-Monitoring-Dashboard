package com.covidtracker.api.repository;

import com.covidtracker.api.mapper.CovidDataRowMapper;
import com.covidtracker.api.model.CovidData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Repository for accessing COVID-19 data from MySQL database
 * Uses JdbcTemplate to execute SQL queries and HikariCP to manage database connections
 */
@Repository
public class CovidDataRepository {

    private static final Logger logger = LoggerFactory.getLogger(CovidDataRepository.class);

    private final JdbcTemplate jdbcTemplate;

    public CovidDataRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        logger.info("CovidDataRepository initialized with JDBC Template");
    }

    /**
     * Returns all country data from database
     * 
     * Called by:
     * - GET /api/countries (via CovidDataServiceImpl.getAllCountriesData())
     * - GET /api/global (via CovidDataServiceImpl.getGlobalStats())
     * 
     * @return List of all CovidData objects
     */
    public List<CovidData> findAllCountryData() {
        logger.debug("Fetching all countries from database");
        String query = "SELECT id, country, total_cases, new_cases, total_deaths, new_deaths, " +
                       "total_recovered, active_cases, critical_cases, last_updated, " +
                       "created_at, updated_at " +
                       "FROM covid_data " +
                       "ORDER BY country ASC";
        return jdbcTemplate.query(query, new CovidDataRowMapper());
    }

    /**
     * Searches for a country by name (case-insensitive)
     * 
     * Called by:
     * - GET /api/country/{name} (via CovidDataServiceImpl.getCountryData())
     * 
     * @param countryName Name of the country to search for
     * @return CovidData object if found, null otherwise
     */
    public CovidData findByCountryName(String countryName) {
        logger.debug("Searching for country: {}", countryName);
        String query = "SELECT id, country, total_cases, new_cases, total_deaths, new_deaths, " +
                       "total_recovered, active_cases, critical_cases, last_updated, " +
                       "created_at, updated_at " +
                       "FROM covid_data " +
                       "WHERE country = ? " +
                       "LIMIT 1";
        
        List<CovidData> results = jdbcTemplate.query(query, new CovidDataRowMapper(), countryName);
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Bulk update or insert - Updates or inserts multiple CovidData records
     * Uses INSERT ... ON DUPLICATE KEY UPDATE
     * @Transactional ensures atomicity: all succeed or all rollback
     * 
     * Called by:
     * - POST /api/refresh (via CovidDataServiceImpl.refreshCovidData())
     * - Application startup (via CovidDataInitializer.loadInitialData())
     * 
     * @param dataList List of CovidData objects to update or insert
     * @return Total number of rows affected
     */
    @Transactional
    public int bulkUpdateOrInsertCovidData(List<CovidData> dataList) {
        logger.info("Bulk updating or inserting {} country records", dataList.size());
        String query = "INSERT INTO covid_data " +
                       "(country, total_cases, new_cases, total_deaths, new_deaths, " +
                       "total_recovered, active_cases, critical_cases, last_updated) " +
                       "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                       "ON DUPLICATE KEY UPDATE " +
                       "total_cases = VALUES(total_cases), " +
                       "new_cases = VALUES(new_cases), " +
                       "total_deaths = VALUES(total_deaths), " +
                       "new_deaths = VALUES(new_deaths), " +
                       "total_recovered = VALUES(total_recovered), " +
                       "active_cases = VALUES(active_cases), " +
                       "critical_cases = VALUES(critical_cases), " +
                       "last_updated = VALUES(last_updated), " +
                       "updated_at = CURRENT_TIMESTAMP";
        
        int totalRowsAffected = 0;
        
        for (CovidData data : dataList) {
            try {
                int rowsAffected = jdbcTemplate.update(query,
                    data.getCountry(),
                    data.getTotalCases(),
                    data.getNewCases(),
                    data.getTotalDeaths(),
                    data.getNewDeaths(),
                    data.getTotalRecovered(),
                    data.getActiveCases(),
                    data.getCriticalCases(),
                    data.getLastUpdated()
                );
                totalRowsAffected += rowsAffected;
            } catch (Exception e) {
                logger.error("Error updating or inserting data for country: {}", data.getCountry(), e);
                throw e; // Transaction will rollback
            }
        }
        
        logger.info("Bulk update or insert completed. {} rows affected", totalRowsAffected);
        return totalRowsAffected;
    }

    /**
     * Checks if database is empty (no records)
     * 
     * Called by:
     * - Application startup (via CovidDataInitializer.run())
     * 
     * @return true if database is empty, false otherwise
     */
    public boolean isEmpty() {
        String countQuery = "SELECT COUNT(*) FROM covid_data";
        Integer count = jdbcTemplate.queryForObject(countQuery, Integer.class);
        return count == null || count == 0;
    }
}
