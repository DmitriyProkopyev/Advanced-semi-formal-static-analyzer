package com.infrastructure;

import com.infrastructure.model_types.LLMType;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LLM {
    private final List<ChatLanguageModel> modelInstances;
    private int index;

    public final LLMType type;

    public LLM(Collection<String> keys, LLMType type) {
        this.modelInstances = new ArrayList<>(keys.size());
        this.type = type;
        this.index = 0;

        for (var key : keys) {
            var model = GoogleAiGeminiChatModel.builder()
                    .apiKey(key)
                    .modelName(type.name)
                    .build();
            this.modelInstances.add(model);
        }
    }

    public ChatLanguageModel nextModel() {
        index %= this.modelInstances.size();
        return modelInstances.get(index++);
    }
}
