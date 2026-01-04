package com.covidtracker.api.service.impl;

import com.covidtracker.api.model.CovidData;
import com.covidtracker.api.model.GlobalStats;
import com.covidtracker.api.repository.CovidDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CovidDataServiceImpl
 * Tests business logic in isolation by mocking the repository layer
 */
@ExtendWith(MockitoExtension.class)
class CovidDataServiceImplTest {

    @Mock
    private CovidDataRepository covidDataRepository;

    @InjectMocks
    private CovidDataServiceImpl covidDataService;

    private List<CovidData> testData;

    @BeforeEach
    void setUp() {
        // Create test data: USA and India
        testData = new ArrayList<>();
        CovidData usa = new CovidData();
        usa.setCountry("USA");
        usa.setTotalCases(1000000L);
        usa.setTotalDeaths(20000L);
        usa.setTotalRecovered(900000L);
        usa.setActiveCases(80000L);
        usa.setCriticalCases(10000L);
        testData.add(usa);

        CovidData india = new CovidData();
        india.setCountry("India");
        india.setTotalCases(500000L);
        india.setTotalDeaths(10000L);
        india.setTotalRecovered(450000L);
        india.setActiveCases(40000L);
        india.setCriticalCases(5000L);
        testData.add(india);
    }

    /**
     * Tests global stats aggregation - sums data from all countries
     * Expected: USA (1M cases) + India (500K cases) = 1.5M total cases
     */
    @Test
    void testGetGlobalStats() {
        when(covidDataRepository.findAllCountryData()).thenReturn(testData);

        GlobalStats result = covidDataService.getGlobalStats();

        assertNotNull(result);
        assertEquals(1500000L, result.getTotalCases());  // 1M + 500K
        assertEquals(30000L, result.getTotalDeaths());    // 20K + 10K
        assertEquals(1350000L, result.getTotalRecovered()); // 900K + 450K
        assertEquals(120000L, result.getActiveCases());   // 80K + 40K
        assertEquals(15000L, result.getCriticalCases());  // 10K + 5K
        assertEquals(2, result.getAffectedCountries());
        assertNotNull(result.getLastUpdated());
        
        verify(covidDataRepository, times(1)).findAllCountryData();
    }

    /**
     * Tests null handling - ensures null values are treated as 0
     */
    @Test
    void testGetGlobalStatsWithNullValues() {
        CovidData dataWithNulls = new CovidData();
        dataWithNulls.setCountry("Test");
        dataWithNulls.setTotalCases(null);
        dataWithNulls.setTotalDeaths(null);
        
        List<CovidData> dataWithNullsList = new ArrayList<>();
        dataWithNullsList.add(dataWithNulls);
        
        when(covidDataRepository.findAllCountryData()).thenReturn(dataWithNullsList);

        GlobalStats result = covidDataService.getGlobalStats();

        assertNotNull(result);
        assertEquals(0L, result.getTotalCases());
        assertEquals(0L, result.getTotalDeaths());
    }

    /**
     * Tests retrieval of all countries data
     */
    @Test
    void testGetAllCountriesData() {
        when(covidDataRepository.findAllCountryData()).thenReturn(testData);

        List<CovidData> result = covidDataService.getAllCountriesData();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("USA", result.get(0).getCountry());
        assertEquals("India", result.get(1).getCountry());
        
        verify(covidDataRepository, times(1)).findAllCountryData();
    }

    /**
     * Tests retrieval of specific country data
     */
    @Test
    void testGetCountryData() {
        when(covidDataRepository.findByCountryName("USA")).thenReturn(testData.get(0));

        CovidData result = covidDataService.getCountryData("USA");

        assertNotNull(result);
        assertEquals("USA", result.getCountry());
        assertEquals(1000000L, result.getTotalCases());
        
        verify(covidDataRepository, times(1)).findByCountryName("USA");
    }

    /**
     * Tests behavior when country is not found (returns null)
     */
    @Test
    void testGetCountryDataNotFound() {
        when(covidDataRepository.findByCountryName("NonExistent")).thenReturn(null);

        CovidData result = covidDataService.getCountryData("NonExistent");

        assertNull(result);
        verify(covidDataRepository, times(1)).findByCountryName("NonExistent");
    }

}
