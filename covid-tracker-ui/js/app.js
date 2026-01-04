/**
 * COVID-19 Live Tracker Dashboard - Main Application Logic
 * 
 * This file handles:
 * - Fetching data from REST API endpoints
 * - Rendering global statistics and country data
 * - Search and sort functionality
 * - Error handling and loading states
 */

// ============================================
// Configuration
// ============================================

// API Base URL is now loaded from config.js
// Update config.js for different environments (local/AWS)

// Store countries data globally for filtering and sorting
let countriesData = [];
let sortDirection = 'asc';
let sortColumnIndex = -1;

// ============================================
// Application Initialization
// ============================================

/**
 * Initialize the application on page load
 */
document.addEventListener('DOMContentLoaded', function() {
    console.log('COVID-19 Tracker Dashboard initialized');
    loadAllData();
});

/**
 * Load all data (global stats and countries)
 */
function loadAllData() {
    fetchGlobalStats();
    fetchCountriesData();
}

/**
 * Refresh data on button click
 */
function refreshData() {
    const refreshBtn = document.getElementById('refresh-btn');
    refreshBtn.disabled = true;
    refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Refreshing...';
    
    loadAllData();
    
    // Re-enable button after 2 seconds
    setTimeout(() => {
        refreshBtn.disabled = false;
        refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Refresh';
    }, 2000);
}

// ============================================
// Global Statistics Functions
// ============================================

/**
 * Fetch global COVID-19 statistics from API
 */
async function fetchGlobalStats() {
    const loadingDiv = document.getElementById('global-loading');
    const cardsDiv = document.getElementById('global-stats-cards');
    
    // Show loading state
    loadingDiv.style.display = 'block';
    cardsDiv.style.display = 'none';
    
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/global`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        
        const data = await response.json();
        renderGlobalStats(data);
        
        // Hide loading, show cards
        loadingDiv.style.display = 'none';
        cardsDiv.style.display = 'flex';
        
    } catch (error) {
        console.error('Error fetching global stats:', error);
        loadingDiv.style.display = 'none';
        showError('Failed to load global statistics. Please check if the API server is running.');
    }
}

/**
 * Render global statistics in the UI
 * @param {Object} data - Global stats data from API
 */
function renderGlobalStats(data) {
    // Update stat cards with formatted numbers
    document.getElementById('global-total-cases').textContent = formatNumber(data.totalCases);
    document.getElementById('global-total-deaths').textContent = formatNumber(data.totalDeaths);
    document.getElementById('global-total-recovered').textContent = formatNumber(data.totalRecovered);
    document.getElementById('global-active-cases').textContent = formatNumber(data.activeCases);
    
    // Update last updated timestamp
    if (data.lastUpdated) {
        document.getElementById('last-updated').textContent = `Last Updated: ${data.lastUpdated}`;
    }
}

// ============================================
// Countries Data Functions
// ============================================

/**
 * Fetch country-wise COVID-19 data from API
 */
async function fetchCountriesData() {
    const loadingDiv = document.getElementById('countries-loading');
    const tableContainer = document.getElementById('countries-table-container');
    
    // Show loading state
    loadingDiv.style.display = 'block';
    tableContainer.style.display = 'none';
    
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/countries`);
        
        if (!response.ok) {
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        
        const data = await response.json();
        countriesData = data;
        renderCountriesTable(countriesData);
        
        // Hide loading, show table
        loadingDiv.style.display = 'none';
        tableContainer.style.display = 'block';
        
    } catch (error) {
        console.error('Error fetching countries data:', error);
        loadingDiv.style.display = 'none';
        showError('Failed to load country data. Please check if the API server is running.');
    }
}

/**
 * Render countries data in the table
 * @param {Array} countries - Array of country data objects
 */
function renderCountriesTable(countries) {
    const tbody = document.getElementById('countries-table-body');
    const noResults = document.getElementById('no-results');
    
    // Clear existing rows
    tbody.innerHTML = '';
    
    if (countries.length === 0) {
        noResults.style.display = 'block';
        return;
    }
    
    noResults.style.display = 'none';
    
    // Create table rows for each country
    countries.forEach((country, index) => {
        const row = document.createElement('tr');
        
        row.innerHTML = `
            <td class="text-muted">${index + 1}</td>
            <td><strong>${escapeHtml(country.country)}</strong></td>
            <td class="text-end text-number">${formatNumber(country.totalCases)}</td>
            <td class="text-end text-number">
                ${country.newCases > 0 
                    ? `<span class="badge bg-warning text-dark">+${formatNumber(country.newCases)}</span>` 
                    : formatNumber(country.newCases)}
            </td>
            <td class="text-end text-number text-danger">${formatNumber(country.totalDeaths)}</td>
            <td class="text-end text-number text-success">${formatNumber(country.totalRecovered)}</td>
            <td class="text-end text-number text-warning">${formatNumber(country.activeCases)}</td>
        `;
        
        tbody.appendChild(row);
    });
}

// ============================================
// Search and Filter Functions
// ============================================

/**
 * Filter countries based on search input
 */
function filterCountries() {
    const searchInput = document.getElementById('search-input').value.toLowerCase();
    
    const filteredCountries = countriesData.filter(country => 
        country.country.toLowerCase().includes(searchInput)
    );
    
    renderCountriesTable(filteredCountries);
}

// ============================================
// Table Sorting Functions
// ============================================

/**
 * Sort table by column
 * @param {number} columnIndex - Index of the column to sort by
 */
function sortTable(columnIndex) {
    // Column 0 is "#" (row number), not sortable - adjust index
    const actualColumnIndex = columnIndex;
    
    // Toggle sort direction if clicking the same column
    if (sortColumnIndex === actualColumnIndex) {
        sortDirection = sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        sortDirection = 'asc';
        sortColumnIndex = actualColumnIndex;
    }
    
    // Define sort keys for each column (column 0 is "#", so start from index 1)
    const sortKeys = [null, 'country', 'totalCases', 'newCases', 'totalDeaths', 'totalRecovered', 'activeCases'];
    const sortKey = sortKeys[columnIndex];
    
    // Don't sort if clicking on "#" column
    if (!sortKey) {
        return;
    }
    
    // Sort the countries data
    countriesData.sort((a, b) => {
        let aValue = a[sortKey];
        let bValue = b[sortKey];
        
        // Handle string comparison for country names
        if (sortKey === 'country') {
            aValue = aValue.toLowerCase();
            bValue = bValue.toLowerCase();
            return sortDirection === 'asc' 
                ? aValue.localeCompare(bValue)
                : bValue.localeCompare(aValue);
        }
        
        // Handle numeric comparison
        return sortDirection === 'asc' 
            ? (aValue || 0) - (bValue || 0)
            : (bValue || 0) - (aValue || 0);
    });
    
    // Re-render the table with sorted data
    renderCountriesTable(countriesData);
}

// ============================================
// Error Handling Functions
// ============================================

/**
 * Display error message to user
 * @param {string} message - Error message to display
 */
function showError(message) {
    const errorAlert = document.getElementById('error-alert');
    const errorMessage = document.getElementById('error-message');
    
    errorMessage.textContent = message;
    errorAlert.style.display = 'block';
    errorAlert.classList.add('show');
    
    // Auto-hide after 10 seconds
    setTimeout(() => {
        closeErrorAlert();
    }, 10000);
}

/**
 * Close error alert
 */
function closeErrorAlert() {
    const errorAlert = document.getElementById('error-alert');
    errorAlert.classList.remove('show');
    setTimeout(() => {
        errorAlert.style.display = 'none';
    }, 300);
}

// ============================================
// Utility Functions
// ============================================

/**
 * Format large numbers with commas
 * @param {number} num - Number to format
 * @returns {string} Formatted number string
 */
function formatNumber(num) {
    if (num === null || num === undefined) {
        return '--';
    }
    return num.toLocaleString('en-US');
}

/**
 * Escape HTML to prevent XSS attacks
 * @param {string} text - Text to escape
 * @returns {string} Escaped text
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ============================================
// Export functions for testing (if needed)
// ============================================

// Uncomment if you need to test these functions
// module.exports = { formatNumber, escapeHtml, sortTable };

