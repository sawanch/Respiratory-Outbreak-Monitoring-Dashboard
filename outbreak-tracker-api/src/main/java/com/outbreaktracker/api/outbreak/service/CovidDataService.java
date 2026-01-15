package com.outbreaktracker.api.outbreak.service;

import com.outbreaktracker.api.outbreak.model.CovidData;
import com.outbreaktracker.api.outbreak.model.GlobalStats;

import java.util.List;

/**
 * Service interface defining respiratory outbreak data operations
 * Implementation: CovidDataServiceImpl
 */
public interface CovidDataService {

    /**
     * Aggregates and returns worldwide outbreak statistics
     */
    GlobalStats getGlobalStats();

    /**
     * Returns outbreak data for all countries
     */
    List<CovidData> getAllCountriesData();

    /**
     * Returns outbreak data for a specific country (case-insensitive)
     * @param countryName Country name to search for
     * @return CovidData if found, null otherwise
     */
    CovidData getCountryData(String countryName);

    /**
     * Reloads data from CSV file without restarting application
     */
    void refreshCovidData();
}
