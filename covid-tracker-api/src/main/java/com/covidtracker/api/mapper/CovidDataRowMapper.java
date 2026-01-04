package com.covidtracker.api.mapper;

import com.covidtracker.api.model.CovidData;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * RowMapper for converting ResultSet rows to CovidData objects
 */
public class CovidDataRowMapper implements RowMapper<CovidData> {

    @Override
    public CovidData mapRow(ResultSet rs, int rowNum) throws SQLException {
        CovidData data = new CovidData();
        
        data.setCountry(rs.getString("country"));
        data.setTotalCases(getLongOrNull(rs, "total_cases"));
        data.setNewCases(getLongOrNull(rs, "new_cases"));
        data.setTotalDeaths(getLongOrNull(rs, "total_deaths"));
        data.setNewDeaths(getLongOrNull(rs, "new_deaths"));
        data.setTotalRecovered(getLongOrNull(rs, "total_recovered"));
        data.setActiveCases(getLongOrNull(rs, "active_cases"));
        data.setCriticalCases(getLongOrNull(rs, "critical_cases"));
        data.setLastUpdated(rs.getString("last_updated"));
        
        return data;
    }

    /**
     * Safely gets Long value from ResultSet, handling NULL values
     */
    private Long getLongOrNull(ResultSet rs, String columnName) throws SQLException {
        long value = rs.getLong(columnName);
        return rs.wasNull() ? null : value;
    }
}

