package iu.sna.stack_analysis.analyzers;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import iu.sna.stack_analysis.core.AnalysisResult;
import iu.sna.stack_analysis.core.BasicAnalyzer;
import iu.sna.stack_analysis.interfaces.IAnalyzer;

public class ScanCodeAnalyzer extends BasicAnalyzer implements IAnalyzer {
    private static final ObjectMapper mapper = new ObjectMapper();

    public ScanCodeAnalyzer() {
        super("ScanCode");
    }

    @Override
    public AnalysisResult analyze(File projectDir) {
        AnalysisResult result = new AnalysisResult();
        File scanCodeReport = null;

        try {
            scanCodeReport = runScanCode(projectDir);

            if (scanCodeReport == null || !scanCodeReport.exists()) {
                System.err.println("ScanCode report not found.");
                return result;
            }

            // try to read output
            Map<String, Object> scanData = mapper.readValue(scanCodeReport, Map.class);

            parseLanguages(scanData, result);
            parseLicenses(scanData, result);
            parseTechnologies(scanData, result);

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

        return result;
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
    

    private void parseLanguages(Map<String, Object> scanData, AnalysisResult result) {
        Map<String, Double> languages = new HashMap<>();
        
        // try to obtain info about stack from packages
        List<Map<String, Object>> packages = (List<Map<String, Object>>) scanData.get("packages");
        if (packages != null) {
            for (Map<String, Object> pkg : packages) {
                String lang = (String) pkg.get("primary_language");
                if (lang != null) {
                    languages.merge(lang, 1.0, Double::sum);
                }
            }
        }
        
        if (!languages.isEmpty()) {
            double total = languages.values().stream().mapToDouble(Double::doubleValue).sum();
            languages.replaceAll((k, v) -> (v / total) * 100);
            result.getLanguagePercentages().putAll(languages);
        }
    }

    private void parseLicenses(Map<String, Object> scanData, AnalysisResult result) {
        Map<String, String> licenses = result.getLicenses();
        
        // Обрабатываем пакеты
        List<Map<String, Object>> packages = (List<Map<String, Object>>) scanData.get("packages");
        if (packages != null) {
            for (Map<String, Object> pkg : packages) {
                String pkgName = (String) pkg.get("name");
                String license = (String) pkg.get("declared_license_expression");
                if (pkgName != null && license != null) {
                    licenses.put(pkgName, license);
                }
            }
        }
        
        List<Map<String, Object>> files = (List<Map<String, Object>>) scanData.get("files");
        if (files != null) {
            for (Map<String, Object> file : files) {
                List<Map<String, Object>> fileLicenses = (List<Map<String, Object>>) file.get("licenses");
                if (fileLicenses != null && !fileLicenses.isEmpty()) {
                    String fileName = (String) file.get("path");
                    String license = (String) fileLicenses.get(0).get("key");
                    licenses.put(fileName, license);
                }
            }
        }
    }

    private void parseTechnologies(Map<String, Object> scanData, AnalysisResult result) {
        Map<String, String> technologies = result.getTechnologies();
    
        List<Map<String, Object>> packages = (List<Map<String, Object>>) scanData.get("packages");
        if (packages != null) {
            for (Map<String, Object> pkg : packages) {
                String type = (String) pkg.get("type");
                String name = (String) pkg.get("name");
                String version = (String) pkg.get("version");
                String namespace = (String) pkg.get("namespace");
    
                if (type != null && name != null) {
                    String fullName = namespace != null ? namespace + "/" + name : name;
                    String techKey = type + ":" + fullName;
                    technologies.put(techKey, version != null ? version : "unknown");
                }
            }
        }
    
        // Parse deps (PURLs)
        List<Map<String, Object>> deps = (List<Map<String, Object>>) scanData.get("dependencies");
        if (deps != null) {
            for (Map<String, Object> dep : deps) {
                String purl = (String) dep.get("purl");
                if (purl != null && purl.startsWith("pkg:")) {
                    try {
                        String[] purlParts = purl.substring(4).split("/");
                        String type = purlParts[0];
                        String namePart = purlParts.length > 2 ? purlParts[1] + "/" + purlParts[2] : purlParts[1];
                        String[] nameVer = namePart.split("@");
                        String name = nameVer[0];
                        String version = nameVer.length > 1 ? nameVer[1] : "unknown";
    
                        String techKey = type + ":" + name;
                        technologies.putIfAbsent(techKey, version);
                    } catch (Exception e) {
                        System.err.println("Failed to parse purl: " + purl);
                    }
                }
            }
        }
    }    
}