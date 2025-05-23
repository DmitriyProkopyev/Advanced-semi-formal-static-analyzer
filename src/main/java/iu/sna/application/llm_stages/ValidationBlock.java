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
    }

    public String unify(Collection<String> criticism) {
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
    }

    private String generateInstructions() {
        // generate detailed actionable instructions
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
    }
}
