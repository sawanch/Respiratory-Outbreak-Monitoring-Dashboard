package com.outbreaktracker.api.aiinsights.model;

import java.util.List;

/**
 * Model representing a group of targeted precautions for a specific demographic
 */
public class PrecautionGroup {
    
    private String group;  // e.g., "🏃 Athletes & Sports Personnel"
    private List<String> tips;
    
    public PrecautionGroup() {
    }
    
    public PrecautionGroup(String group, List<String> tips) {
        this.group = group;
        this.tips = tips;
    }
    
    public String getGroup() {
        return group;
    }
    
    public void setGroup(String group) {
        this.group = group;
    }
    
    public List<String> getTips() {
        return tips;
    }
    
    public void setTips(List<String> tips) {
        this.tips = tips;
    }
    
    @Override
    public String toString() {
        return "PrecautionGroup{" +
                "group='" + group + '\'' +
                ", tips=" + tips +
                '}';
    }
}
