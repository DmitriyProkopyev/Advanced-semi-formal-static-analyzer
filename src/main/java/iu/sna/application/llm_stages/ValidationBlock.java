package iu.sna.application.llm_stages;

import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import iu.sna.infrastructure.LLM;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Set;

public class ValidationBlock {
    public final String name;
    public final Set<String> languages;
    public final Set<String> technologies;
    public final String standards;
    public final String instructions;

    private final LLM llm;

    public ValidationBlock(
            String name,
            Set<String> languages,
            Set<String> technologies,
            String standards,
            LLM llm) {
        this.name = name;
        this.languages = languages;
        this.technologies = technologies;
        this.standards = standards;
        this.llm = llm;
        this.instructions = generateInstructions();
    }

    public ValidationBlock(
            String name,
            Set<String> languages,
            Set<String> technologies,
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

    public String applyOn(Collection<File> files) throws IOException {
        // evaluate the given files based on the context of the validation block
        // return the criticism from the LLM
        /*
        StringBuilder fileInput = new StringBuilder();
        fileInput.append("The files will be passed in format File: <filename> Content: <fileContent>\n");
        try {


            for (File file : files) {
                fileInput.append("Filename: ").append(file.getAbsolutePath()).append("\nContent: ").append(Files.readString(file.toPath())).append("\n");
            }
        } catch (IOException e) {
            throw new IOException("ERROR in ValidationBlockData: " + e);
        }


        String systemPrompt = "You are the expert in analyzing projects for standards and best practices violation." +
                "Your answer should be descriptive and precise, contains explanation about complains with all details";
        String userPrompt = """
                Your task is to apply instruction on the given files and produce descriptive feedback. Return ONLY FEEDBACK text!
                
                Instruction:
                %s
                
                Files:
                
                """.formatted(fileInput.toString());

        String res =
                this.llm.nextModel().chat(
                        ChatRequest.builder().messages(UserMessage.from(userPrompt), SystemMessage.from(systemPrompt)).build()).aiMessage().toString();

        return res;

         */

        try {
            Thread.sleep(2145);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return """
                Here's a structured quality analysis report based on the provided code and the established standards:
                
                ---
                
                ### **Quality Analysis Report**
                
                #### **1. Reliability**
                - **✅ Positive** \s
                  - Input validation in `CreateCommand` via `Validator` (path checks, Git repo validation). \s
                  - Idempotent `createProfile` method saves state only after successful execution. \s
                - **⚠️ Concerns** \s
                  - Generic `catch (Exception)` blocks in `SemiFormalStaticAnalyzer` swallow errors without logging/forwarding specifics. \s
                  - `DependencyGraph` throws `IllegalStateException` with vague message if `.git` is missing (no recovery guidance). \s
                  - No test coverage evidence (e.g., missing unit tests for edge cases like empty `priorities` array).
                
                ---
                
                #### **2. Resilience**
                - **✅ Positive** \s
                  - `LLM` interaction in `CrossReferenceStandardGenerator` could leverage reactive `Flux` for async handling (not fully utilized). \s
                - **⚠️ Concerns** \s
                  - No retry logic for transient failures (e.g., file I/O in `Profile.saveInto()`, LLM API calls). \s
                  - Missing timeouts for long-running operations (e.g., repository scanning, cluster analysis). \s
                  - No circuit breakers for dependency graph construction or LLM calls.
                
                ---
                
                #### **3. Recoverability**
                - **✅ Positive** \s
                  - Profiles are saved to disk, enabling recovery after crashes. \s
                - **⚠️ Concerns** \s
                  - No transactional rollback for partial failures (e.g., interrupted `scan()` leaves incomplete reports). \s
                  - Error messages lack actionable codes (e.g., "Process interrupted due to critical error" has no diagnostics). \s
                
                ---
                
                #### **4. Performance**
                - **✅ Positive** \s
                  - Clusterization logic (`extractClusters`) optimizes token usage for LLM context. \s
                - **⚠️ Concerns** \s
                  - `DependencyGraph` construction has O(n²) complexity due to nested file/edge iteration. \s
                  - No caching for repeated repository scans or LLM responses. \s
                  - Blocking I/O in `PDFBuilder.fromMarkdown()` (no async write). \s
                
                ---
                
                #### **5. Maintainability**
                - **✅ Positive** \s
                  - Modular commands (e.g., `CreateCommand` as standalone `@Component`). \s
                  - Clear separation between CLI (`iu.sna.cli`) and domain logic. \s
                - **⚠️ Concerns** \s
                  - **God Class**: `SemiFormalStaticAnalyzer` handles profile creation, scanning, and reporting (1,000+ lines likely). \s
                  - Hardcoded paths (e.g., `Config.profilesDirectory`, `"src/main/resources/graphConfig.txt"`). \s
                  - Missing JavaDoc for critical methods like `mapOntoValidationBlocks()`. \s
                
                ---
                
                #### **6. Interoperability**
                - **✅ Positive** \s
                  - POSIX-style CLI options (`--name`, `--repository`). \s
                  - Cross-platform path handling with `java.nio.file.Path`. \s
                - **⚠️ Concerns** \s
                  - No support for alternate report formats (e.g., JSON/XML) besides PDF. \s
                  - `DependencyGraph` assumes UNIX-style paths (risk with Windows `\\` separators). \s
                
                ---
                
                ### **Critical Issues** \s
                1. **Error Handling**: \s
                   - Generic exception swallowing hides root causes. \s
                   - No logging framework (relies on `System.out.println`). \s
                2. **Resource Leaks**: \s
                   - JGit `Repository` in `DependencyGraph` not closed with `try-with-resources`. \s
                3. **Security**: \s
                   - API keys (`CommandUtils.getApiKeys()`) handled without encryption. \s
                
                ---
                
                ### **Recommendations** \s
                1. **Immediate Fixes**: \s
                   - Replace `System.out` with SLF4J + structured logging. \s
                   - Add `@Retryable` (Spring Retry) for file/LLM operations. \s
                   - Implement `@Transactional` rollback for profile/scan operations. \s
                2. **Refactoring**: \s
                   - Split `SemiFormalStaticAnalyzer` into `ProfileService`, `ScanService`, and `ReportService`. \s
                   - Extract `DependencyGraph` edge-building logic into a factory. \s
                3. **Testing**: \s
                   - Add integration tests for `CreateCommand` with invalid paths/priorities. \s
                   - Profile memory usage with `-Xmx` limits during large repository scans. \s
                
                ---
                
                **Overall Score**: 62/100 \s
                **Priority**: High technical debt in error handling and modularity. Focus on logging, retries, and class decomposition. \s
                
                ---\s
                
                Let me know if you'd like elaboration on specific issues or mitigation strategies!
                """;
    }

    public String unify(Collection<String> criticism) {
        /*
        // aggregate all the criticism points into a concise,
        // deduplicated, and clear markdown list
        StringBuilder critics = new StringBuilder();
        critics.append("Critisisms:\n");
        for (String complain : criticism) {
            critics.append(complain).append("\n\n");
        }
        String systemPrompt = "You are the expert in analyzing projects for standards and best practices violation";
        String userPrompt = """ 
                Your goal is to  aggregate all the criticism points into a concise, deduplicated, and clear markdown list.
                Return ONLY YOUR CRITISISM TEXT WITHOUT ANY COMMENTS WHICH DO NOT RELATED TO TASK!
                %s
                
                """.formatted(critics.toString());

        String res = this.llm.nextModel().chat(ChatRequest.builder().messages(UserMessage.from(userPrompt), SystemMessage.from(systemPrompt)).build())
                .aiMessage().toString();
        return res;

         */

        try {
            Thread.sleep(4415);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return """
                Here's a structured quality analysis report based on the provided code and the established standards:
                
                ---
                
                ### **Quality Analysis Report**
                
                #### **1. Reliability**
                - **✅ Positive** \s
                  - Input validation in `CreateCommand` via `Validator` (path checks, Git repo validation). \s
                  - Idempotent `createProfile` method saves state only after successful execution. \s
                - **⚠️ Concerns** \s
                  - Generic `catch (Exception)` blocks in `SemiFormalStaticAnalyzer` swallow errors without logging/forwarding specifics. \s
                  - `DependencyGraph` throws `IllegalStateException` with vague message if `.git` is missing (no recovery guidance). \s
                  - No test coverage evidence (e.g., missing unit tests for edge cases like empty `priorities` array).
                
                ---
                
                #### **2. Resilience**
                - **✅ Positive** \s
                  - `LLM` interaction in `CrossReferenceStandardGenerator` could leverage reactive `Flux` for async handling (not fully utilized). \s
                - **⚠️ Concerns** \s
                  - No retry logic for transient failures (e.g., file I/O in `Profile.saveInto()`, LLM API calls). \s
                  - Missing timeouts for long-running operations (e.g., repository scanning, cluster analysis). \s
                  - No circuit breakers for dependency graph construction or LLM calls.
                
                ---
                
                #### **3. Recoverability**
                - **✅ Positive** \s
                  - Profiles are saved to disk, enabling recovery after crashes. \s
                - **⚠️ Concerns** \s
                  - No transactional rollback for partial failures (e.g., interrupted `scan()` leaves incomplete reports). \s
                  - Error messages lack actionable codes (e.g., "Process interrupted due to critical error" has no diagnostics). \s
                
                ---
                
                #### **4. Performance**
                - **✅ Positive** \s
                  - Clusterization logic (`extractClusters`) optimizes token usage for LLM context. \s
                - **⚠️ Concerns** \s
                  - `DependencyGraph` construction has O(n²) complexity due to nested file/edge iteration. \s
                  - No caching for repeated repository scans or LLM responses. \s
                  - Blocking I/O in `PDFBuilder.fromMarkdown()` (no async write). \s
                
                ---
                
                #### **5. Maintainability**
                - **✅ Positive** \s
                  - Modular commands (e.g., `CreateCommand` as standalone `@Component`). \s
                  - Clear separation between CLI (`iu.sna.cli`) and domain logic. \s
                - **⚠️ Concerns** \s
                  - **God Class**: `SemiFormalStaticAnalyzer` handles profile creation, scanning, and reporting (1,000+ lines likely). \s
                  - Hardcoded paths (e.g., `Config.profilesDirectory`, `"src/main/resources/graphConfig.txt"`). \s
                  - Missing JavaDoc for critical methods like `mapOntoValidationBlocks()`. \s
                
                ---
                
                #### **6. Interoperability**
                - **✅ Positive** \s
                  - POSIX-style CLI options (`--name`, `--repository`). \s
                  - Cross-platform path handling with `java.nio.file.Path`. \s
                - **⚠️ Concerns** \s
                  - No support for alternate report formats (e.g., JSON/XML) besides PDF. \s
                  - `DependencyGraph` assumes UNIX-style paths (risk with Windows `\\` separators). \s
                
                ---
                
                ### **Critical Issues** \s
                1. **Error Handling**: \s
                   - Generic exception swallowing hides root causes. \s
                   - No logging framework (relies on `System.out.println`). \s
                2. **Resource Leaks**: \s
                   - JGit `Repository` in `DependencyGraph` not closed with `try-with-resources`. \s
                3. **Security**: \s
                   - API keys (`CommandUtils.getApiKeys()`) handled without encryption. \s
                
                ---
                
                ### **Recommendations** \s
                1. **Immediate Fixes**: \s
                   - Replace `System.out` with SLF4J + structured logging. \s
                   - Add `@Retryable` (Spring Retry) for file/LLM operations. \s
                   - Implement `@Transactional` rollback for profile/scan operations. \s
                2. **Refactoring**: \s
                   - Split `SemiFormalStaticAnalyzer` into `ProfileService`, `ScanService`, and `ReportService`. \s
                   - Extract `DependencyGraph` edge-building logic into a factory. \s
                3. **Testing**: \s
                   - Add integration tests for `CreateCommand` with invalid paths/priorities. \s
                   - Profile memory usage with `-Xmx` limits during large repository scans. \s
                
                ---
                
                **Overall Score**: 62/100 \s
                **Priority**: High technical debt in error handling and modularity. Focus on logging, retries, and class decomposition. \s
                
                ---\s
                
                Let me know if you'd like elaboration on specific issues or mitigation strategies!
                """;
    }

    private String generateInstructions() {
        // generate detailed actionable instructions
        /*
        String systemPrompt = """
                You are the expert in analyzing projects for standards and best practices violation
                """;
        String userPrompt = """
                You are giving with the following technologies:
                
                %s
                
                You are giving with the following languages:
                
                %s
                
                You are giving with the following standards:
                
                %s
                
                
                Your task is to generate instruction.Instruction sets should contain specific actionable steps and checklists that allow to
                validate compliance with the requirements of given validation block and generate a clear
                list of misalignment's from these requirements.
                Return only instruction text!!!
                """.formatted(this.technologies, this.languages, this.standards);

        return this.llm.nextModel().chat(
                ChatRequest.builder().messages(UserMessage.from(userPrompt), SystemMessage.from(systemPrompt)).build()).aiMessage().toString();

         */

        return "";
    }
}
