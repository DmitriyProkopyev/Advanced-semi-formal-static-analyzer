package iu.sna.application;

import iu.sna.application.llm_stages.ValidationBlock;
import iu.sna.domain.repository_scanner.FileTechnologyStack;

import java.util.Collection;
import java.util.Map;

public class ValidationBlockBuilder {
    public ValidationBlockBuilder(Collection<FileTechnologyStack> techStack) {
        // decide the grouping logic here based on the frequency of language-to-tech occurrences

    }

    public Collection<ValidationBlock> buildFrom(Map<String, String> languages,
                                                 Map<String, String> technologies) {
        // use the grouping logic to group together languages and technologies
        // use an LLM to rewrite the standards into groups
        return null;
    }
}
