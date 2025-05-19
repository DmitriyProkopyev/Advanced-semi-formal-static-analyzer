package com.domain.repository_scanner.stack_analysis_core.analyzers;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.nio.file.Paths;
import java.nio.file.Files;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.InputStream;

import com.domain.repository_scanner.FileTechnologyStack;
import com.domain.repository_scanner.stack_analysis_core.core.AnalysisResult;
import com.domain.repository_scanner.stack_analysis_core.core.BasicAnalyzer;
import com.domain.repository_scanner.stack_analysis_core.interfaces.IAnalyzer;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ScanCodeAnalyzer extends BasicAnalyzer implements IAnalyzer {
    private static final ObjectMapper mapper = new ObjectMapper();

    // Храним все зависимости проекта
    public static Set<String> allProjectDependencies = new HashSet<>();

    private static Map<String, String> EXT_TO_LANG = null;

    private static void loadExtensionToLanguage() {
        if (EXT_TO_LANG != null) return;
        try {
            InputStream is = ScanCodeAnalyzer.class.getClassLoader().getResourceAsStream("com/domain/repository_scanner/stack_analysis_core/config/ScanCodeConfig.json");
            if (is != null) {
                Map<String, Object> config = mapper.readValue(is, new TypeReference<Map<String, Object>>(){});
                Object extLangObj = config.get("extension_to_language");
                if (extLangObj instanceof Map<?,?> extLangMap) {
                    EXT_TO_LANG = new HashMap<>();
                    for (var entry : extLangMap.entrySet()) {
                        EXT_TO_LANG.put(entry.getKey().toString(), entry.getValue().toString());
                    }
                } else {
                    EXT_TO_LANG = Map.of();
                }
            } else {
                EXT_TO_LANG = Map.of();
            }
        } catch (Exception e) {
            EXT_TO_LANG = Map.of();
            System.err.println("Could not load extension_to_language from config: " + e.getMessage());
        }
    }

    public ScanCodeAnalyzer() {
        super("ScanCode");
    }

    @Override
    public AnalysisResult analyze(File projectDir) {
        return new AnalysisResult();
    }

    public List<FileTechnologyStack> analyzeFiles(File projectDir) {
        File scanCodeReport = null;
        try {
            scanCodeReport = runScanCode(projectDir);
            if (scanCodeReport == null || !scanCodeReport.exists()) {
                System.err.println("ScanCode report not found.");
                return new ArrayList<>();
            }
            return parseResult(scanCodeReport);
        } catch (IOException | InterruptedException e) {
            System.err.println("ScanCode execution failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (scanCodeReport != null && scanCodeReport.exists()) {
                if (!scanCodeReport.delete()) {
                    System.err.println("Warning: could not delete temp file " + scanCodeReport.getAbsolutePath());
                }
            }
        }
        return new ArrayList<>();
    }

    private File runScanCode(File projectDir) throws IOException, InterruptedException {
        File outputFile = File.createTempFile("scancode_results", ".json");
        outputFile.deleteOnExit(); // Clean temp file
    
        // Launch ScanCode from CLI
        ProcessBuilder pb = new ProcessBuilder(
            "scancode",
            "--package",
            "--json-pp",
            outputFile.getAbsolutePath(),
            projectDir.getAbsolutePath()
        );
    
        pb.directory(projectDir);
    
        // Ignore stdout of the console
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.DISCARD);
    
        Process process = pb.start();
    
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new IOException("ScanCode failed with exit code " + exitCode);
        }
    
        return outputFile;
    }

    public static List<FileTechnologyStack> parseResult(File jsonFile) throws IOException {
        loadExtensionToLanguage();
        Map<String, Object> scanData = mapper.readValue(jsonFile, Map.class);
        List<FileTechnologyStack> result = new ArrayList<>();

        //  Collect technologies from packages
        Map<String, Set<String>> fileToTechnologies = new HashMap<>();
        Map<String, String> packageUidToTech = new HashMap<>();
        List<Map<String, Object>> packages = (List<Map<String, Object>>) scanData.get("packages");
        if (packages != null) {
            for (Map<String, Object> pkg : packages) {
                String lang = (String) pkg.get("primary_language");
                String type = (String) pkg.get("type");
                String name = (String) pkg.get("name");
                String version = (String) pkg.get("version");
                String packageUid = (String) pkg.get("package_uid");
                List<String> datafilePaths = (List<String>) pkg.get("datafile_paths");
                String tech = type + ":" + (name != null ? name : "") + (version != null ? ("@" + version) : "");
                if (packageUid != null) {
                    packageUidToTech.put(packageUid, tech);
                }
                if (datafilePaths != null) {
                    for (String path : datafilePaths) {
                        fileToTechnologies.computeIfAbsent(path, k -> new HashSet<>()).add(tech);
                    }
                }
            }
        }

        // 1. Собираем зависимости из dependencies
        Map<String, Set<String>> fileToDependencies = new HashMap<>();
        allProjectDependencies = new HashSet<>();
        List<Map<String, Object>> dependencies = (List<Map<String, Object>>) scanData.get("dependencies");
        if (dependencies != null) {
            for (Map<String, Object> dep : dependencies) {
                String depTech = (String) dep.get("purl");
                if (depTech == null) depTech = (String) dep.get("dependency_uid");
                if (depTech == null) continue;
                allProjectDependencies.add(depTech);
                String datafile = (String) dep.get("datafile_path");
                if (datafile != null) {
                    fileToDependencies.computeIfAbsent(datafile, k -> new HashSet<>()).add(depTech);
                }
            }
        }

        //  Collect languages and technologies from files
        List<Map<String, Object>> files = (List<Map<String, Object>>) scanData.get("files");
        if (files != null) {
            for (Map<String, Object> file : files) {
                String path = (String) file.get("path");
                if (path == null) continue;
                // Skip directories
                if ("directory".equals(file.get("type"))) continue;
                String language = null;
                if (file.containsKey("programming_language")) {
                    Object langObj = file.get("programming_language");
                    if (langObj instanceof Map) {
                        Object langName = ((Map<?, ?>) langObj).get("name");
                        if (langName != null) language = langName.toString();
                    } else if (langObj instanceof String) {
                        if (langObj != null) language = (String) langObj;
                    }
                }
                // If language is not defined, try to get it from the extension using hash map
                if ((language == null || language.isBlank()) && path != null) {
                    String fileName = new File(path).getName().toLowerCase();
                    int dot = fileName.lastIndexOf('.');
                    String ext = (dot != -1) ? fileName.substring(dot) : "";
                    System.out.println("splitted file: " + path + " to " + fileName + " and " + ext);
                    language = EXT_TO_LANG.getOrDefault(ext, null);
                    System.out.println(EXT_TO_LANG);
                }
                // Package technologies
                Set<String> technologies = new HashSet<>(fileToTechnologies.getOrDefault(path, new HashSet<>()));
                // Добавляем зависимости для файла
                technologies.addAll(fileToDependencies.getOrDefault(path, new HashSet<>()));
                // Add technologies from for_packages
                List<String> forPackages = (List<String>) file.get("for_packages");
                if (forPackages != null) {
                    for (String pkgUid : forPackages) {
                        String tech = packageUidToTech.get(pkgUid);
                        if (tech != null) {
                            technologies.add(tech);
                        }
                    }
                }
                // Create FileTechnologyStack
                FileTechnologyStack stack = new FileTechnologyStack(new File(path), language, new ArrayList<>(technologies));
                result.add(stack);
            }
        }
        return result;
    }

    /**
     * Pretty prints the list of FileTechnologyStack
     */
    public static void print_pisun(Collection<FileTechnologyStack> stacks) {
        // Сначала общий стек
        Set<String> allLangs = new HashSet<>();
        Set<String> allTechs = new HashSet<>();
        for (FileTechnologyStack stack : stacks) {
            if (stack.language() != null) allLangs.add(stack.language());
            allTechs.addAll(stack.technologies());
        }
        allTechs.addAll(allProjectDependencies); // добавить зависимости в общий стек
        System.out.println("=== Project Technology Stack Summary ===");
        System.out.println("Languages detected: " + String.join(", ", allLangs));
        System.out.println("Technologies detected: " + String.join(", ", allTechs));
        System.out.println("Dependencies detected: " + String.join(", ", allProjectDependencies));
        System.out.println("Total files analyzed: " + stacks.size());
        System.out.println("----------------------------------------");
        // Затем подробная инфа по каждому файлу
        for (FileTechnologyStack stack : stacks) {
            System.out.println("File: " + stack.file().getAbsolutePath());
            System.out.println("Language: " + stack.language());
            System.out.println("Technologies: " + String.join(", ", stack.technologies()));
            System.out.println("----------------------------------------");
        }
    }
}