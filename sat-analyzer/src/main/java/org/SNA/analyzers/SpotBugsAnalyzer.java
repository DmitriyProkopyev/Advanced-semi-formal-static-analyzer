package org.SNA.analyzers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.SNA.core.ToolResult;
import org.SNA.core.exceptions.AnalysisException;
import org.SNA.core.interfaces.IAnalysisTool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SpotBugsAnalyzer implements IAnalysisTool {
    @Override
    public String getName() {
        return "SpotBugs";
    }

    @Override
    public ToolResult analyze(String projectPath) throws AnalysisException {
        try {
            // Temp XML file for report
            Path outputXml = Files.createTempFile("spotbugs-report", ".xml");
            runSpotBugs(projectPath, outputXml.toString());

            List<String> messages = parseXmlReport(outputXml.toFile());
            return new ToolResult(getName(), messages.size(), 0, messages);
        } catch (IOException | InterruptedException e) {
            throw new AnalysisException("SpotBugs execution failed: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new AnalysisException("Failed to parse SpotBugs report: " + e.getMessage() + e.getCause(), e);
        }
    }

    private void runSpotBugs(String targetPath, String outputXml) throws IOException, InterruptedException {
        List<String> command = Arrays.asList(
                "spotbugs",
                "-textui",
                "-effort:max",
                "-low",
                "-xml",
                "-output", outputXml,
                targetPath
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            reader.lines().forEach(System.out::println);
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("SpotBugs exited with code " + exitCode);
        }
    }

    private List<String> parseXmlReport(File xmlFile) throws Exception {
        List<String> messages = new ArrayList<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true); // Fix security issue
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(xmlFile);

        NodeList bugInstances = doc.getElementsByTagName("BugInstance");

        for (int i = 0; i < bugInstances.getLength(); i++) {
            Element bug = (Element) bugInstances.item(i);
            String type = bug.getAttribute("type");
            String message = bug.getAttribute("message");
            String category = bug.getAttribute("category");

            Element sourceLine = (Element) bug.getElementsByTagName("SourceLine").item(0);
            String file = sourceLine.getAttribute("sourcepath");
            String line = sourceLine.getAttribute("start");

            String formatted = String.format("%s:%s - %s (%s)", file, line, type, category, message);
            messages.add(formatted);
        }

        return messages;
    }
}