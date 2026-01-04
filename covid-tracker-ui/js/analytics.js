/**
 * API Analytics Dashboard - Main Application Logic
 */

// Chart instances
let endpointStatsChart = null;
let responseTimeChart = null;
let successErrorChart = null;

// Timeline data for sorting
let timelineData = [];
let timelineSortDirection = 'desc';
let timelineSortColumnIndex = 0;

// Utility: Remove /api prefix from endpoint
function formatEndpoint(ep) {
    return ep.startsWith('/api') ? ep.substring(4) : ep;
}

// Initialize on page load
document.addEventListener('DOMContentLoaded', function() {
    console.log('[Analytics] Dashboard initialized');
    loadAnalytics();
});

// Load analytics data
async function loadAnalytics() {
    try {
        showLoading(true);
        
        const [summaryResponse, timelineResponse] = await Promise.all([
            fetch(`${CONFIG.API_BASE_URL}/analytics/summary`),
            fetch(`${CONFIG.API_BASE_URL}/analytics/timeline`)
        ]);

        if (!summaryResponse.ok || !timelineResponse.ok) {
            throw new Error('Failed to fetch analytics data');
        }

        const summary = await summaryResponse.json();
        const timeline = await timelineResponse.json();

        console.log('[Analytics] Data loaded:', { 
            totalRequests: summary.totalRequests, 
            timelineCount: timeline.length 
        });

        displaySummary(summary);
        displayTimeline(timeline);
        updateLastUpdated();
        showLoading(false);
    } catch (error) {
        console.error('[Analytics] Error loading data:', error);
        showError('Failed to load analytics data. Make sure the API is running.');
        showLoading(false);
    }
}

// Display summary in charts
function displaySummary(summary) {
    document.getElementById('total-requests').textContent = summary.totalRequests || 0;
    createEndpointStatsChart(summary.endpointStats || {});
    createResponseTimeChart(summary.responseTimeStats || {});
    createSuccessErrorChart(summary.successErrorRates || {});
}

// Create endpoint stats chart
function createEndpointStatsChart(endpointStats) {
    const endpoints = Object.keys(endpointStats).map(formatEndpoint);
    const counts = Object.values(endpointStats);
    
    endpointStatsChart = Highcharts.chart('endpoint-stats-chart', {
        chart: { type: 'column', height: 300, backgroundColor: 'transparent' },
        credits: { enabled: false },
        title: { text: '' },
        xAxis: { 
            categories: endpoints,
            labels: { 
                style: { fontSize: '11px' },
                rotation: -45
            }
        },
        yAxis: { 
            min: 0,
            title: { text: 'Request Count', style: { fontSize: '12px', fontWeight: '600' } },
            labels: { style: { fontSize: '11px' } }
        },
        legend: { enabled: false },
        series: [{ data: counts, color: '#36a2eb' }],
        exporting: { enabled: false }
    });
}

// Create response time chart
function createResponseTimeChart(responseTimeStats) {
    const endpoints = Object.keys(responseTimeStats).map(formatEndpoint);
    const times = Object.values(responseTimeStats).map(t => Math.round(t * 100) / 100);
    
    responseTimeChart = Highcharts.chart('response-time-chart', {
        chart: { type: 'line', height: 300, backgroundColor: 'transparent' },
        credits: { enabled: false },
        title: { text: '' },
        xAxis: { 
            categories: endpoints,
            labels: { 
                style: { fontSize: '11px' },
                rotation: -45
            }
        },
        yAxis: { 
            title: { text: 'Response Time (ms)', style: { fontSize: '12px', fontWeight: '600' } },
            labels: { style: { fontSize: '11px' } }
        },
        legend: { align: 'center', verticalAlign: 'top', itemStyle: { fontSize: '12px' } },
        tooltip: { enabled: false },
        plotOptions: { 
            line: { 
                dataLabels: { enabled: true, style: { fontSize: '10px' } },
                enableMouseTracking: false
            }
        },
        series: [{ name: 'Average Response Time', data: times, color: '#17a2b8' }],
        exporting: { enabled: false }
    });
}

// Create success/error chart
function createSuccessErrorChart(successErrorRates) {
    const endpoints = Object.keys(successErrorRates);
    const successData = [];
    const errorData = [];
    
    endpoints.forEach(endpoint => {
        const rates = successErrorRates[endpoint];
        successData.push(rates.success || 0);
        errorData.push(rates.error || 0);
    });
    
    const displayEndpoints = endpoints.map(formatEndpoint);
    
    successErrorChart = Highcharts.chart('success-error-chart', {
        chart: { type: 'column', height: 300, backgroundColor: 'transparent' },
        credits: { enabled: false },
        title: { text: '' },
        xAxis: { 
            categories: displayEndpoints,
            labels: { 
                style: { fontSize: '11px' },
                rotation: -45
            }
        },
        yAxis: { 
            min: 0,
            title: { text: 'Request Count', style: { fontSize: '12px', fontWeight: '600' } },
            labels: { style: { fontSize: '11px' } }
        },
        legend: { align: 'center', verticalAlign: 'top', itemStyle: { fontSize: '12px' } },
        plotOptions: { column: { stacking: 'normal' } },
        series: [
            { name: 'Success (2xx)', data: successData, color: '#28a745' }, 
            { name: 'Errors (4xx/5xx)', data: errorData, color: '#dc3545' }
        ],
        exporting: { enabled: false }
    });
}

// Display timeline table
function displayTimeline(timeline) {
    timelineData = timeline || [];
    // Sort by timestamp descending (latest first) on initial load
    timelineSortColumnIndex = -1; // Set to -1 so first sort doesn't toggle
    timelineSortDirection = 'desc';
    sortTimelineTable(1); // Column 1 is timestamp (0 is row number)
}

// Sort timeline table
function sortTimelineTable(columnIndex) {
    // Adjust columnIndex since we added row number column (column 0 is now row number)
    const actualColumnIndex = columnIndex;
    
    if (timelineSortColumnIndex === actualColumnIndex) {
        timelineSortDirection = timelineSortDirection === 'asc' ? 'desc' : 'asc';
    } else {
        // Default to desc for timestamp (column 1), asc for others
        timelineSortDirection = actualColumnIndex === 1 ? 'desc' : 'asc';
        timelineSortColumnIndex = actualColumnIndex;
    }
    
    timelineData.sort((a, b) => {
        let aValue, bValue;
        
        switch(actualColumnIndex) {
            case 1: // Timestamp
                aValue = new Date(a.timestamp).getTime();
                bValue = new Date(b.timestamp).getTime();
                break;
            case 2: // Endpoint
                aValue = a.endpoint.toLowerCase();
                bValue = b.endpoint.toLowerCase();
                return timelineSortDirection === 'asc' 
                    ? aValue.localeCompare(bValue)
                    : bValue.localeCompare(aValue);
            case 3: // Method
                aValue = a.method.toLowerCase();
                bValue = b.method.toLowerCase();
                return timelineSortDirection === 'asc' 
                    ? aValue.localeCompare(bValue)
                    : bValue.localeCompare(aValue);
            case 4: // Status Code
                aValue = a.statusCode || 0;
                bValue = b.statusCode || 0;
                break;
            case 5: // Response Time
                aValue = a.responseTime || 0;
                bValue = b.responseTime || 0;
                break;
            default:
                return 0;
        }
        
        return timelineSortDirection === 'asc' ? aValue - bValue : bValue - aValue;
    });
    
    renderTimelineTable();
}

// Render timeline table
function renderTimelineTable() {
    const timelineBody = document.getElementById('timeline-body');
    timelineBody.innerHTML = '';
    
    if (timelineData && timelineData.length > 0) {
        timelineData.forEach((entry, index) => {
            const row = document.createElement('tr');
            const timestamp = new Date(entry.timestamp).toLocaleString();
            const statusClass = entry.statusCode >= 200 && entry.statusCode < 300 ? 'text-success' : 
                               entry.statusCode >= 400 ? 'text-danger' : 'text-warning';
            
            row.innerHTML = `
                <td class="text-muted">${index + 1}</td>
                <td>${timestamp}</td>
                <td>${entry.endpoint}</td>
                <td><span class="badge bg-secondary">${entry.method}</span></td>
                <td class="text-end ${statusClass}">${entry.statusCode}</td>
                <td class="text-end">${entry.responseTime}</td>
            `;
            timelineBody.appendChild(row);
        });
    } else {
        timelineBody.innerHTML = '<tr><td colspan="6" class="text-center text-muted">No requests recorded yet</td></tr>';
    }
}

// Refresh analytics
function refreshAnalytics() {
    console.log('[Analytics] Refreshing data...');
    const refreshBtn = document.querySelector('button[onclick="refreshAnalytics()"]');
    if (refreshBtn) {
        refreshBtn.disabled = true;
        refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Refreshing...';
    }
    
    loadAnalytics();
    
    setTimeout(() => {
        if (refreshBtn) {
            refreshBtn.disabled = false;
            refreshBtn.innerHTML = '<i class="bi bi-arrow-clockwise"></i> Refresh';
        }
    }, 2000);
}

// Show/hide loading
function showLoading(show) {
    document.getElementById('loading').style.display = show ? 'block' : 'none';
    document.getElementById('analytics-content').style.display = show ? 'none' : 'block';
}

// Show error message
function showError(message) {
    const errorAlert = document.getElementById('error-alert');
    const errorMessage = document.getElementById('error-message');
    errorMessage.textContent = message;
    errorAlert.style.display = 'block';
    errorAlert.classList.add('show');
    
    setTimeout(() => {
        closeErrorAlert();
    }, 10000);
}

// Close error alert
function closeErrorAlert() {
    const errorAlert = document.getElementById('error-alert');
    errorAlert.classList.remove('show');
    setTimeout(() => {
        errorAlert.style.display = 'none';
    }, 300);
}

// Update last updated timestamp
function updateLastUpdated() {
    const now = new Date();
    const formattedDate = now.toLocaleString('en-US', {
        month: 'long',
        day: 'numeric',
        year: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true,
        timeZone: 'UTC'
    }) + ' UTC';
    const lastUpdatedElement = document.getElementById('last-updated');
    if (lastUpdatedElement) {
        lastUpdatedElement.textContent = `Last Updated: ${formattedDate}`;
    }
}
