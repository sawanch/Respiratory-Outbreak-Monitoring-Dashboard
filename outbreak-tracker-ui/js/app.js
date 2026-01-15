/**
 * Respiratory Outbreak Monitoring Dashboard - Main Application Logic
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
    console.log('Respiratory Outbreak Monitoring Dashboard initialized');
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
 * Fetch global respiratory outbreak statistics from API
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
 * Fetch country-wise respiratory outbreak data from API
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
        populateCountryDropdown(countriesData);
        
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

/**
 * Populate country dropdown for AI Insights
 * @param {Array} countries - Array of country data objects
 */
function populateCountryDropdown(countries) {
    const select = document.getElementById('ai-country-select');
    
    // Clear existing options except the first one (placeholder)
    select.innerHTML = '<option value="">Select a country...</option>';
    
    // Sort countries alphabetically
    const sortedCountries = [...countries].sort((a, b) => 
        a.country.localeCompare(b.country)
    );
    
    // Add country options
    sortedCountries.forEach(country => {
        const option = document.createElement('option');
        option.value = country.country;
        option.textContent = country.country;
        select.appendChild(option);
    });
    
    // Set US as default and load insights automatically
    select.value = 'US';
    getOutbreakInsights();
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
// AI Insights Functions
// ============================================

/**
 * Get AI-powered respiratory outbreak insights for a country
 */
async function getOutbreakInsights() {
    const countrySelect = document.getElementById('ai-country-select');
    const countryInput = countrySelect.value.trim();
    
    if (!countryInput) {
        showError('Please select a country to get AI insights.');
        return;
    }
    
    const container = document.getElementById('ai-insights-container');
    
    // Show loading state
    container.innerHTML = `
        <div class="col-12">
            <div class="card border-0 shadow-sm">
                <div class="card-body ai-loading">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">Loading...</span>
                    </div>
                    <p class="ai-loading-text">Generating AI insights for ${escapeHtml(countryInput)}...</p>
                </div>
            </div>
        </div>
    `;
    
    try {
        const response = await fetch(`${CONFIG.API_BASE_URL}/ai-insights/country/${encodeURIComponent(countryInput)}`);
        
        if (!response.ok) {
            if (response.status === 404) {
                throw new Error(`Country "${countryInput}" not found. Please check the spelling and try again.`);
            }
            throw new Error(`HTTP error! Status: ${response.status}`);
        }
        
        const data = await response.json();
        renderOutbreakInsights(data);
        
    } catch (error) {
        console.error('Error fetching AI insights:', error);
        container.innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger" role="alert">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                    <strong>Error:</strong> ${escapeHtml(error.message)}
                </div>
            </div>
        `;
    }
}

/**
 * Render AI insights in the UI with 2-column layout
 * @param {Object} data - Respiratory outbreak insights data from API
 */
function renderOutbreakInsights(data) {
    const container = document.getElementById('ai-insights-container');
    
    // Calculate percentages
    const activeCasePercent = data.totalCases > 0 
        ? ((data.activeCases / data.totalCases) * 100).toFixed(1) 
        : '0.0';
    const dailyIncreasePercent = data.totalCases > 0 
        ? ((data.newCases / data.totalCases) * 100).toFixed(3) 
        : '0.0';
    
    // Build targeted precautions HTML (with cards)
    let precautionsHTML = '';
    if (data.targetedPrecautions && data.targetedPrecautions.length > 0) {
        const precautionCards = data.targetedPrecautions.map(group => `
            <div class="precaution-card">
                <h6 class="precaution-card-title">${escapeHtml(group.group)}</h6>
                <ul class="precaution-card-list">
                    ${group.tips.map(tip => `<li>${escapeHtml(tip)}</li>`).join('')}
                </ul>
            </div>
        `).join('');
        precautionsHTML = `<div class="precautions-container">${precautionCards}</div>`;
    }
    
    // Build main info card with 2-column layout
    let html = `
        <div class="col-12">
            <div class="insight-main-card">
                <h3><i class="bi bi-geo-alt-fill"></i> ${escapeHtml(data.country)} - AI-Powered Analysis</h3>
                
                <div class="row mt-4">
                    <!-- LEFT COLUMN: Key Metrics & Assessment -->
                    <div class="col-md-4">
                        <div class="metrics-container">
                            <!-- Active Cases Card -->
                            <div class="metric-card">
                                <div class="metric-icon">⚠️</div>
                                <div class="metric-content">
                                    <div class="metric-label">Active Cases</div>
                                    <div class="metric-value">${formatNumber(data.activeCases)}</div>
                                    <div class="metric-subtext">(${activeCasePercent}% of total cases)</div>
                                </div>
                            </div>
                            
                            <!-- New Cases Card -->
                            <div class="metric-card">
                                <div class="metric-icon">📈</div>
                                <div class="metric-content">
                                    <div class="metric-label">New Cases Today</div>
                                    <div class="metric-value">${formatNumber(data.newCases)}</div>
                                    <div class="metric-subtext">(${dailyIncreasePercent}% daily increase)</div>
                                </div>
                            </div>
                        </div>
                        
                        <!-- AI Assessment -->
                        <div class="mt-4 assessment-section">
                            <strong style="font-size: 1.15rem;"><i class="bi bi-lightbulb-fill"></i> AI Assessment:</strong>
                            <p class="mt-3 assessment-text-content" style="font-size: 1.1rem; line-height: 1.8; opacity: 0.95;">${escapeHtml(data.overallAssessment)}</p>
                        </div>
                    </div>
                    
                    <!-- RIGHT COLUMN: AI Metadata & Precautions -->
                    <div class="col-md-7 offset-md-1">
                        <!-- AI Metadata -->
                        <div class="ai-metadata mb-4">
                            <div class="ai-metadata-header"><i class="bi bi-robot"></i> AI-Generated Analysis</div>
                            <div class="ai-metadata-details"><strong>Model:</strong> GPT-4o-mini | <strong>Temperature:</strong> 0.7 | <strong>Tokens:</strong> ~800</div>
                        </div>
                        
                        <h5 class="precautions-header mb-3"><i class="bi bi-shield-check"></i> Precautions</h5>
                        ${precautionsHTML || '<p class="text-white-50">No specific precautions available.</p>'}
                    </div>
                </div>
                
                <!-- Timestamp -->
                <div class="mt-4" style="font-size: 1.05rem; opacity: 0.85; border-top: 1px solid rgba(255,255,255,0.2); padding-top: 14px;">
                    <i class="bi bi-clock"></i> Generated: ${escapeHtml(data.generatedAt)}
                </div>
            </div>
        </div>
    `;
    
    container.innerHTML = html;
}

// ============================================
// Export functions for testing (if needed)
// ============================================

// Uncomment if you need to test these functions
// module.exports = { formatNumber, escapeHtml, sortTable, getOutbreakInsights, renderOutbreakInsights };
