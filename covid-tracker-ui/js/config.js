/**
 * Configuration file for API endpoints
 * 
 * Toggle between environments by commenting/uncommenting the appropriate lines:
 * - Local Development: Use http://localhost:8080/api (for testing with local backend)
 * - AWS Production: Use http://3.95.130.213:8080/api (for deployed application)
 * 
 * Note: Ensure backend API is running before starting the UI
 */
const CONFIG = {
    // API Base URL - Toggle between local and AWS
    // API_BASE_URL: 'http://3.95.130.213:8080/api',  // AWS Production
    API_BASE_URL: 'http://localhost:8080/api',        // Local Development
    
    // Swagger UI URL - Toggle between local and AWS
    // SWAGGER_UI_URL: 'http://3.95.130.213:8080/swagger-ui/index.html'  // AWS Production
    SWAGGER_UI_URL: 'http://localhost:8080/swagger-ui/index.html'        // Local Development
};