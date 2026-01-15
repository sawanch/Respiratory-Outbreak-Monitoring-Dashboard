package com.outbreaktracker.api.analytics.model;

/**
 * Individual insight card for analytics recommendations
 * Represents a single AI-generated recommendation
 */
public class AnalyticsInsightCard {
    
    private String icon;
    private String title;
    private String description;
    private String severity; // info, warning, success
    
    // Constructors
    public AnalyticsInsightCard() {}
    
    public AnalyticsInsightCard(String icon, String title, String description, String severity) {
        this.icon = icon;
        this.title = title;
        this.description = description;
        this.severity = severity;
    }

    // Getters and Setters
    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }
}
