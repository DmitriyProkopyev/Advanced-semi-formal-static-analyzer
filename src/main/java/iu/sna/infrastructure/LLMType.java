package iu.sna.infrastructure;

import iu.sna.application.Config;

import java.util.stream.Stream;

public enum LLMType {
    gemini(Config.gemini.getString("name"),
            Config.gemini.getInt("max_input_tokens"),
            Config.gemini.getInt("max_output_tokens"),
            Config.gemini.getInt("daily_requests_per_key"),
            Config.gemini.getInt("concurrent_sessions_per_key"));

    public final String name;
    public final int maxInputTokens;
    public final int maxOutputTokens;
    public final int dailyRequestsPerKey;
    public final int concurrentSessionsPerKey;

    LLMType(String name,
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

    public static LLMType getLLMType(String name) {
        return Stream.of(values())
                .filter(value -> value.name().equals(name))
                .findFirst()
                .orElse(null);
    }
}
