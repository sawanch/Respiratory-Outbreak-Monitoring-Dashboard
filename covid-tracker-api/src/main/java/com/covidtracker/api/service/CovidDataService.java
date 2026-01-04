package com.covidtracker.api.service;

import com.covidtracker.api.model.CovidData;
import com.covidtracker.api.model.GlobalStats;

import java.util.List;

/**
 * Service interface defining COVID-19 data operations
 * Implementation: CovidDataServiceImpl
 */
public interface CovidDataService {

    /**
     * Aggregates and returns worldwide COVID-19 statistics
     */
    GlobalStats getGlobalStats();

    /**
     * Returns COVID-19 data for all countries
     */
    List<CovidData> getAllCountriesData();

    /**
     * Returns COVID-19 data for a specific country (case-insensitive)
     * @param countryName Country name to search for
     * @return CovidData if found, null otherwise
     */
    CovidData getCountryData(String countryName);

    /**
     * Reloads data from CSV file without restarting application
     */
    void refreshCovidData();
}
