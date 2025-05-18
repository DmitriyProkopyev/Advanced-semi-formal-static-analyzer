package iu.sna.application.llm_stages;

import iu.sna.infrastructure.LLM;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;

public class CrossReferenceStandardGenerator {
    private final String prompt;

    public CrossReferenceStandardGenerator(LLM llm,
                                           Collection<String> abstractStandards,
                                           Collection<Path> context) {
        this.prompt = "";
        // load the prompt and combine with abstract standards
        // use context
    }

    public CrossReferenceStandardGenerator(Collection<String> abstractStandards) {
        this.prompt = ""; // simply load the prompt
    }

    public Map<String, String> generateFor(Collection<String> technologies) {
        // use LLM to generate technology-specific standard
        // return standards for each technology
        return null;
    }
}
