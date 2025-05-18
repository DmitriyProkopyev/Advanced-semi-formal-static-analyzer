package com.infrastructure.model_types;

import com.application.Config;

public enum LLMType {
    Gemini(Config.gemini.getString("name"),
            Config.gemini.getInt("max_input_tokens"),
            Config.gemini.getInt("max_output_tokens"),
            Config.gemini.getInt("daily_requests_per_key"),
            Config.gemini.getInt("concurrent_sessions_per_key"));

    public final String name;
    public final int maxInputTokens;
    public final int maxOutputTokens;
    public final int dailyRequestsPerKey;
    public final int concurrentSessionsPerKey;

    private LLMType(String name,
                    int maxInputTokens,
                    int maxOutputTokens,
                    int dailyRequestsPerKey,
                    int concurrentSessionsPerKey) {
        this.name = name;
        this.maxInputTokens = maxInputTokens;
        this.maxOutputTokens = maxOutputTokens;
        this.dailyRequestsPerKey = dailyRequestsPerKey;
        this.concurrentSessionsPerKey = concurrentSessionsPerKey;
    }
}
