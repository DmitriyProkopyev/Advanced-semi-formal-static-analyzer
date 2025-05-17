package org.example.analyzer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MadgeAnalyzer {
    private final Path projectRoot;
    private final Set<String> supportedExtensions;
    private static final Pattern DEPENDENCY_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*->\\s*\"([^\"]+)\"");

    public MadgeAnalyzer(Path projectRoot) {
        this.projectRoot = projectRoot;
        this.supportedExtensions = new HashSet<>(Arrays.asList("ts", "js"));
    }

    public Map<String, Set<String>> analyzeDependencies() throws IOException {
        // Создаем временный файл для DOT вывода
        File dotFile = File.createTempFile("dependencies", ".dot");
        dotFile.deleteOnExit();

        // Запускаем Madge и сохраняем результат в DOT файл
        ProcessBuilder processBuilder = new ProcessBuilder(
            "npx", "madge", 
            "--dot",
            "--extensions", String.join(",", supportedExtensions),
            projectRoot.toString()
        );
        processBuilder.redirectOutput(dotFile);
        processBuilder.directory(projectRoot.toFile());
        
        Process process = processBuilder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Madge analysis was interrupted", e);
        }

        // Парсим DOT файл и создаем карту зависимостей
        Map<String, Set<String>> dependencies = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new java.io.FileInputStream(dotFile)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Matcher matcher = DEPENDENCY_PATTERN.matcher(line);
                if (matcher.find()) {
                    String source = matcher.group(1);
                    String target = matcher.group(2);
                    
                    dependencies.computeIfAbsent(source, k -> new HashSet<>()).add(target);
                }
            }
        }

        return dependencies;
    }

    public Set<String> findCircularDependencies() throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            "npx", "madge",
            "--circular",
            "--extensions", String.join(",", supportedExtensions),
            projectRoot.toString()
        );
        processBuilder.directory(projectRoot.toFile());
        
        Process process = processBuilder.start();
        Set<String> circularDeps = new HashSet<>();
        
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("Circular dependency detected")) {
                    circularDeps.add(line);
                }
            }
        }

        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Circular dependency analysis was interrupted", e);
        }

        return circularDeps;
    }

    public void generateDependencyGraph(String outputPath) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder(
            "npx", "madge",
            "--image", outputPath,
            "--extensions", String.join(",", supportedExtensions),
            projectRoot.toString()
        );
        processBuilder.directory(projectRoot.toFile());
        
        Process process = processBuilder.start();
        try {
            process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Graph generation was interrupted", e);
        }
    }
} 