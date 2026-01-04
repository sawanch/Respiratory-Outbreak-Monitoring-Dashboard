-- This query creates the covid_data table
USE covid_tracker;

-- Drop table if exists (for clean setup)
DROP TABLE IF EXISTS covid_data;

-- Create covid_data table
CREATE TABLE covid_data (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    country VARCHAR(100) NOT NULL UNIQUE,
    total_cases BIGINT DEFAULT 0,
    new_cases BIGINT DEFAULT 0,
    total_deaths BIGINT DEFAULT 0,
    new_deaths BIGINT DEFAULT 0,
    total_recovered BIGINT DEFAULT 0,
    active_cases BIGINT DEFAULT 0,
    critical_cases BIGINT DEFAULT 0,
    last_updated VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Index on country for faster lookups
    INDEX idx_country (country)
);

-- Verify table creation
SHOW TABLES;
DESCRIBE covid_data;