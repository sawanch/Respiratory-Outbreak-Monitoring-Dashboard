package com.covidtracker.api.controller;

import com.covidtracker.api.model.CovidData;
import com.covidtracker.api.model.GlobalStats;
import com.covidtracker.api.service.CovidDataService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CovidDataController
 * Tests HTTP endpoints by mocking the service layer
 */
@ExtendWith(MockitoExtension.class)
class CovidDataControllerTest {

    @Mock
    private CovidDataService covidDataService;

    @InjectMocks
    private CovidDataController covidDataController;

    private GlobalStats mockGlobalStats;
    private List<CovidData> mockCountryList;
    private CovidData mockCovidData;

    @BeforeEach
    void setUp() {
        mockGlobalStats = new GlobalStats(
            1000000L, 20000L, 900000L, 80000L, 10000L, 195, "2024-01-01"
        );

        mockCovidData = new CovidData();
        mockCovidData.setCountry("USA");
        mockCovidData.setTotalCases(100000L);
        mockCovidData.setNewCases(1000L);
        mockCovidData.setTotalDeaths(2000L);
        mockCovidData.setTotalRecovered(90000L);
        mockCovidData.setActiveCases(8000L);
        mockCovidData.setCriticalCases(1000L);

        mockCountryList = new ArrayList<>();
        mockCountryList.add(mockCovidData);
        
        CovidData indiaData = new CovidData();
        indiaData.setCountry("India");
        indiaData.setTotalCases(50000L);
        mockCountryList.add(indiaData);
    }

    /**
     * Tests GET /api/global - returns global statistics
     */
    @Test
    void testGetGlobalStats() {
        when(covidDataService.getGlobalStats()).thenReturn(mockGlobalStats);

        ResponseEntity<GlobalStats> response = covidDataController.getGlobalStats();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1000000L, response.getBody().getTotalCases());
        verify(covidDataService, times(1)).getGlobalStats();
    }

    /**
     * Tests GET /api/countries - returns all countries data
     */
    @Test
    void testGetAllCountries() {
        when(covidDataService.getAllCountriesData()).thenReturn(mockCountryList);

        ResponseEntity<List<CovidData>> response = covidDataController.getAllCountries();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(covidDataService, times(1)).getAllCountriesData();
    }

    /**
     * Tests GET /api/country/{name} - returns country data when found
     * Note: ResponseEntity<?> is used because this endpoint can return either
     * CovidData (200 OK) or Map<String, String> (404 Not Found)
     */
    @Test
    void testGetCountryByName_Success() {
        when(covidDataService.getCountryData("USA")).thenReturn(mockCovidData);

        ResponseEntity<?> response = covidDataController.getCountryByName("USA");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody() instanceof CovidData);
        CovidData body = (CovidData) response.getBody();
        assertEquals("USA", body.getCountry());
        verify(covidDataService, times(1)).getCountryData("USA");
    }

    /**
     * Tests GET /api/country/{name} - returns 404 when country not found
     */
    @Test
    void testGetCountryByName_NotFound() {
        when(covidDataService.getCountryData("InvalidCountry")).thenReturn(null);

        ResponseEntity<?> response = covidDataController.getCountryByName("InvalidCountry");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map);
        
        @SuppressWarnings("unchecked")
        Map<String, String> errorBody = (Map<String, String>) response.getBody();
        assertEquals("Country not found", errorBody.get("error"));
        verify(covidDataService, times(1)).getCountryData("InvalidCountry");
    }

    /**
     * Tests POST /api/refresh - refreshes COVID-19 data from CSV
     */
    @Test
    void testRefreshData() {
        doNothing().when(covidDataService).refreshCovidData();

        ResponseEntity<Map<String, String>> response = covidDataController.refreshData();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("success", response.getBody().get("status"));
        verify(covidDataService, times(1)).refreshCovidData();
    }
}
