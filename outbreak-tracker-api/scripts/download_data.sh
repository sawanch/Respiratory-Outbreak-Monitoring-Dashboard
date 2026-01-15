#!/bin/bash

# ============================================================================
# Respiratory Outbreak Data Download and Refresh Script
# ============================================================================
# Purpose: Automates daily respiratory outbreak data updates from Johns Hopkins
#
# What it does:
#   1. Downloads latest CSV data from Johns Hopkins GitHub repository
#   2. Saves CSV to project's resources/data/ directory
#   3. Triggers Spring Boot app's /api/refresh endpoint to reload data
#
# Usage:
#   Manual: ./scripts/download_data.sh
#   Cron:   0 16 * * * /absolute/path/to/download_data.sh >> logs/cron.log 2>&1
#          (Runs daily at 4 PM, logs output to logs/cron.log)
#
# Requirements:
#   - wget or curl (for downloading CSV)
#   - Spring Boot app running on localhost:8080
#   - Internet connection
# ============================================================================

# Configuration Variables
DATA_URL="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv"
CSV_FILE="src/main/resources/data/covid19_confirmed_global.csv"
API_URL="http://localhost:8080/api/refresh"

# Navigate to project root directory
# This ensures script works regardless of where it's called from
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/.." || {
    echo "Error: Cannot navigate to project root"
    exit 1
}

# Step 1: Download CSV file from Johns Hopkins repository
# -q = quiet mode (wget), -s = silent mode (curl)
# Tries wget first, falls back to curl if wget not available
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Downloading data from Johns Hopkins..."
if wget -q -O "$CSV_FILE" "$DATA_URL" || curl -s -o "$CSV_FILE" "$DATA_URL"; then
    echo "✓ CSV downloaded successfully to: $CSV_FILE"
else
    echo "✗ Download failed. Check your internet connection."
    exit 1
fi

# Step 2: Wait for file system to complete write operation
# Prevents race condition where refresh endpoint might read incomplete file
sleep 2

# Step 3: Trigger Spring Boot app's refresh endpoint
# Tells the app to reload data from the newly downloaded CSV
# -s = silent mode, -X POST = HTTP POST request
echo "[$(date '+%Y-%m-%d %H:%M:%S')] Triggering app refresh..."
if curl -s -X POST "$API_URL" > /dev/null; then
    echo "✓ App refresh triggered successfully"
    echo "✓ Respiratory outbreak data updated!"
else
    echo "✗ Failed to trigger app refresh"
    echo "  Note: Make sure Spring Boot app is running on $API_URL"
    exit 1
fi

echo "[$(date '+%Y-%m-%d %H:%M:%S')] Script completed successfully"
