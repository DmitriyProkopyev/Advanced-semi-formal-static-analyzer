package iu.sna.application;

import com.google.gson.JsonArray;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import iu.sna.application.llm_stages.ValidationBlock;
import iu.sna.domain.repository_scanner.FileTechnologyStack;
import iu.sna.infrastructure.LLM;
import iu.sna.infrastructure.LLMType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class ValidationBlockBuilder {
    private LLM llm;

    public ValidationBlockBuilder(Collection<FileTechnologyStack> techStack, LLM llm) {
        this.llm = llm;
        // decide the grouping logic here based on the frequency of language-to-tech occurrences

    }

    // то есть для каждго языке и технологии мы сделали standard
    public Collection<ValidationBlock> buildFrom(
            Map<String, List<String>> languages,
            Map<String, List<String>> technologies) {
        /*
        String systemPrompt = "You are the expert in analyzing projects for standards and best practices violation";
        String userPrompt = """
                    Standards are any framework, instrument, library, etc.
                
                    You are given with the following Map1 : language -> List of standards
                
                    %s
                
                
                    Map2 : technologies -> List of standards:
                
                    %s
                
                    You task is to group  (belonging to multiple groups is allowed) the standards into validation
                    blocks, each block describing a set of standards that should be validated together.
                    Requirements: 
                    1.  Ensure that the grouping occurs based on expected coupling between code fragments that
                       will be analyzed
                    2. Minimize the number of validation blocks
                    The output should be in format
                    [
                
                      "validation_block_name": {
                      languages: ["language1", "language2", "languageN"],
                      technologies: ["tech1", "tech2", "techN"],
                      standards: ["standard1", "standard2", "standardN"]
                      }
                    ...
                    ]
                    and include only json TEXT (with brackets [])
                """.formatted(languages, technologies);
        String response = this.llm.nextModel()
                .chat(ChatRequest.builder()
                        .messages(UserMessage.from(userPrompt), SystemMessage.from(systemPrompt))
                        .build())
                .aiMessage()
                .toString();
        try {
            JSONArray blocksArray = new JSONArray(response);
            Collection<ValidationBlock> res = new ArrayList<>();
            for (int i = 0; i < blocksArray.length(); i++) {
                JSONObject obj = blocksArray.getJSONObject(i);
                String name = obj.getString("validation_block_name");

                List<String> langs = obj.getJSONArray("languages")
                        .toList()
                        .stream()
                        .map(Object::toString)
                        .toList();

                List<String> techs = obj.getJSONArray("technologies")
                        .toList()
                        .stream()
                        .map(Object::toString)
                        .toList();

                List<String> standards = obj.getJSONArray("standards")
                        .toList()
                        .stream()
                        .map(Object::toString)
                        .toList();

                ValidationBlock block = new ValidationBlock(
                        name,
                        new HashSet<>(langs),
                        new HashSet<>(techs),
                        String.join(" ", standards),
                        this.llm
                );

                res.add(block);
            }
            return res;
        } catch (JSONException e) {
            throw new RuntimeException("Error in ValidationBlockBuilder: " + e);
        }

         */

        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        var blocks = new ArrayList<ValidationBlock>();
        var llm = new LLM(new ArrayList<String>(), LLMType.gemini);

        var instructions = """
                Here's a comprehensive list of quality standards for a Spring Shell-based Java CLI tool, organized by key quality attributes:
                
                ---
                
                ### **1. Reliability**
                - **Input Validation**: Enforce strict validation for CLI arguments/options (e.g., using Spring Shell validators, regex, or custom constraints). \s
                - **Idempotent Commands**: Ensure commands produce the same result when executed multiple times (e.g., safe retries for network operations). \s
                - **Error Detection**: Implement proactive checks for invalid states (e.g., file permissions, network connectivity). \s
                - **Test Coverage**: \s
                  - ≥80% unit test coverage (verified with JaCoCo). \s
                  - Integration tests for end-to-end command execution. \s
                  - Property-based testing for edge cases (e.g., using jqwik). \s
                
                ---
                
                ### **2. Resilience**
                - **Retry Mechanisms**: Use Spring Retry or resilience4j for transient failures (e.g., HTTP timeouts, database disconnects). \s
                - **Timeouts**: Set configurable timeouts for blocking operations (I/O, APIs). \s
                - **Circuit Breakers**: Integrate resilience4j to prevent cascading failures. \s
                - **Resource Guards**: Limit memory/thread usage (e.g., `-Xmx` tuning, `ExecutorService` quotas). \s
                - **Stress Testing**: Validate behavior under high concurrency (e.g., Gatling or custom load runners). \s
                
                ---
                
                ### **3. Recoverability**
                - **Actionable Errors**: Return human-readable messages with error codes and remediation steps (e.g., "Connection failed: retry with `--retries=5`"). \s
                - **State Checkpoints**: Allow saving/restoring session state (e.g., via `@ShellMethod(key = "save-state")`). \s
                - **Transactional Operations**: Roll back partial changes on failure (e.g., atomic file writes). \s
                - **Recovery Scripts**: Provide helper commands to diagnose/undo issues (e.g., `tool diagnose --error-code=503`). \s
                
                ---
                
                ### **4. Performance**
                - **Low Latency**: Optimize startup time with Spring Boot lazy initialization. \s
                - **Resource Efficiency**: Profile memory/CPU with VisualVM or async-profiler; avoid `OutOfMemoryError`. \s
                - **Benchmarks**: Track execution time for critical commands (e.g., with `@Timed` Micrometer metrics). \s
                - **Async Execution**: Offload long-running tasks to background threads (e.g., `@Async`). \s
                - **Caching**: Cache frequent I/O operations (e.g., use Caffeine for in-memory caching). \s
                
                ---
                
                ### **5. Maintainability**
                - **Modular Design**: Split commands into discrete components (e.g., `@ShellComponent` per domain). \s
                - **Code Standards**: Enforce Checkstyle/PMD rules and SOLID principles. \s
                - **Documentation**: \s
                  - Auto-generated help via `spring-shell`'s `help` command. \s
                  - ADRs (Architecture Decision Records) for key design choices. \s
                - **CI/CD**: Include static analysis (SonarQube), dependency checks (OWASP), and versioned releases. \s
                - **Deprecation Policy**: Use `@Deprecated` with migration guides for removed features. \s
                
                ---
                
                ### **6. Interoperability**
                - **POSIX Compliance**: Follow standard CLI conventions (e.g., `--help`, `-v` flags). \s
                - **Cross-Platform Support**: Test on Windows (CMD/PowerShell), Linux, and macOS. \s
                - **Format Support**: Allow input/output in JSON/XML/CSV (e.g., `--format=json`). \s
                - **API Integration**: Expose REST/gRPC endpoints for hybrid workflows (e.g., Spring Boot Actuator). \s
                - **Environment Variables**: Support config via `.env` files or system variables (e.g., `TOOL_API_KEY`). \s
                
                ---
                
                ### **Tools & Integration**
                - **Logging**: Use SLF4J with structured logging (JSON) for Splunk/ELK. \s
                - **Monitoring**: Export metrics to Prometheus/Grafana via Micrometer. \s
                - **Packaging**: Distribute as a standalone JAR with GraalVM native image options. \s
                - **Shell Autocompletion**: Implement TAB-completion for Bash/Zsh (Spring Shell 3.0+ supports this). \s
                
                ---
                
                By adhering to these standards, your CLI tool will align with enterprise-grade expectations while leveraging Spring Shell's capabilities effectively.
                """;

        var block1 = new ValidationBlock("Reliability", new HashSet<String>(),
                new HashSet<String>(), instructions, llm, instructions);
        var block2 = new ValidationBlock("Resilience", new HashSet<String>(),
                new HashSet<String>(), instructions, llm, instructions);
        var block3 = new ValidationBlock("Recoverability", new HashSet<String>(),
                new HashSet<String>(), instructions, llm, instructions);
        var block4 = new ValidationBlock("Performance", new HashSet<String>(),
                new HashSet<String>(), instructions, llm, instructions);
        var block5 = new ValidationBlock("Maintainability", new HashSet<String>(),
                new HashSet<String>(), instructions, llm, instructions);
        var block6 = new ValidationBlock("Interoperability", new HashSet<String>(),
                new HashSet<String>(), instructions, llm, instructions);
        blocks.add(block1);

        return blocks;
        // use the grouping logic to group together languages and technologies
        // use an LLM to rewrite the standards into groups
    }
}
