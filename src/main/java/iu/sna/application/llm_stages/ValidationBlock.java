package iu.sna.application.llm_stages;

import iu.sna.infrastructure.LLM;

import java.io.File;
import java.util.Collection;

public class ValidationBlock {
    public final String name;
    private final Collection<String> languages;
    private final Collection<String> technologies;
    private final String standards;
    private final String instructions;
    private final LLM llm;

    public ValidationBlock(String name,
                           Collection<String> languages,
                           Collection<String> technologies,
                           String standards,
                           LLM llm) {
        this.name = name;
        this.languages = languages;
        this.technologies = technologies;
        this.standards = standards;
        this.llm = llm;
        this.instructions = generateInstructions();
    }

    public ValidationBlock(String name,
                           Collection<String> languages,
                           Collection<String> technologies,
                           String standards,
                           LLM llm,
                           String instructions) {
        this.name = name;
        this.languages = languages;
        this.technologies = technologies;
        this.standards = standards;
        this.llm = llm;
        this.instructions = instructions;
    }

    public String applyOn(Collection<File> files) {
        // evaluate the given files based on the context of the validation block
        // return the criticism from the LLM
        return null;
    }

    public String unify(Collection<String> criticism) {
        // aggregate all the criticism points into a concise,
        // deduplicated, and clear markdown list
        return null;
    }

    private String generateInstructions() {
        // generate detailed actionable instructions
        return null;
    }
}
