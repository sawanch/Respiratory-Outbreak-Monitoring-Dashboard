#!/bin/bash

# ============================================================================
# COVID-19 Data Download and Refresh Script
# ============================================================================
# Purpose: Automates daily COVID-19 data updates from Johns Hopkins
#
# What it does:
#   1. Downloads latest CSV data from Johns Hopkins GitHub repository
#   2. Saves CSV to project's resources/data/ directory
#   3. Triggers Spring Boot app's /api/refresh endpoint to reload data
#
# Usage:
#   Manual: ./scripts/download_covid_data.sh
#   Cron:   0 16 * * * /absolute/path/to/download_covid_data.sh >> logs/cron.log 2>&1
#
# Cron Explanation:
#   0 16 * * * = Every day at 4:00 PM (16:00 in 24-hour format)
#   >> logs/cron.log 2>&1 = Redirects output to log file
#
# Requirements:
#   - wget or curl (for downloading CSV)
#   - Spring Boot app running on localhost:8080
#   - Internet connection
# ============================================================================

# Configuration Variables
JOHNS_HOPKINS_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"
CSV_FILE="src/main/resources/data/covid19_confirmed_global.csv"
API_URL="http://localhost:8080/api/refresh"

# Get script directory and project root (works regardless of where script is called from)
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# Colors for terminal output (only used when run manually, not in cron)
# Cron jobs typically don't have a terminal, so colors won't show in logs
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color (reset)

echo -e "${YELLOW}[$(date '+%Y-%m-%d %H:%M:%S')] Starting COVID-19 data download...${NC}"

# Navigate to project root directory
# This ensures script works regardless of current working directory
cd "$PROJECT_ROOT" || {
    echo -e "${RED}Error: Cannot navigate to project root: $PROJECT_ROOT${NC}"
    exit 1
}

# Step 1: Download CSV file from Johns Hopkins repository
# Uses wget first, falls back to curl if wget is not available
# 2>/dev/null suppresses error messages (we handle errors below)
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Downloading CSV from Johns Hopkins..."
if wget -O "$CSV_FILE" "$JOHNS_HOPKINS_URL" 2>/dev/null || curl -o "$CSV_FILE" "$JOHNS_HOPKINS_URL" 2>/dev/null; then
    echo -e "${GREEN}✓ CSV downloaded successfully to: $CSV_FILE${NC}"
else
    echo -e "${RED}✗ Failed to download CSV from: $JOHNS_HOPKINS_URL${NC}"
    echo -e "${RED}  Check your internet connection and try again${NC}"
    exit 1
fi

# Step 2: Wait for file system to complete write operation
# Prevents race condition where refresh endpoint might read incomplete file
sleep 2

# Step 3: Trigger Spring Boot app's refresh endpoint
# This tells the app to reload data from the newly downloaded CSV
# > /dev/null 2>&1 suppresses output (we only care about exit code)
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Triggering app refresh endpoint..."
if curl -X POST "$API_URL" > /dev/null 2>&1; then
    echo -e "${GREEN}✓ App refresh triggered successfully${NC}"
    echo -e "${GREEN}✓ COVID-19 data updated!${NC}"
else
    echo -e "${RED}✗ Failed to trigger app refresh${NC}"
    echo -e "${YELLOW}  Note: Make sure Spring Boot app is running on $API_URL${NC}"
    echo -e "${YELLOW}  The CSV was downloaded, but app needs to be restarted or refresh endpoint called manually${NC}"
    exit 1
fi

echo -e "${GREEN}[$(date '+%Y-%m-%d %H:%M:%S')] Script completed successfully${NC}"

