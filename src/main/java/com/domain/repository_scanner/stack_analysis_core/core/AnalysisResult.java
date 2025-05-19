package com.domain.repository_scanner.stack_analysis_core.core;

import java.util.HashMap;
import java.util.Map;

public class AnalysisResult {
    private Map<String, Double> languagePercentages = new HashMap<>();
    private Map<String, String> licenses = new HashMap<>();
    private Map<String, String> technologies = new HashMap<>();
    
    public Map<String, Double> getLanguagePercentages() {
        return languagePercentages;
    }

    public Map<String, String> getLicenses() {
        return licenses;
    }

    public Map<String, String> getTechnologies() {
        return technologies;
    }

    public void setLanguagePercentages(Map<String, Double> languagePercentages) {
        this.languagePercentages = languagePercentages;
    }

    public void merge(AnalysisResult other) {
        this.languagePercentages.putAll(other.languagePercentages);
        this.licenses.putAll(other.licenses);
        this.technologies.putAll(other.technologies);
    }
}