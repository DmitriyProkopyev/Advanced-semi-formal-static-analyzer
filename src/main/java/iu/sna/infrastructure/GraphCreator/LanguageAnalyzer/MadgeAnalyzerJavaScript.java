package iu.sna.infrastructure.GraphCreator.LanguageAnalyzer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MadgeAnalyzerJavaScript implements LanguageAnalyzer {
    private static final Pattern DEPENDENCY_PATTERN =
            Pattern.compile("\"([^\"]*)\"\\s*->\\s*\"([^\"]*)\"");

    public List<Map.Entry<Path, Path>> analyzeDependencies(
            List<String> fileList) throws IOException {

        String toolOutput = runTool(fileList);
        Matcher matcher = DEPENDENCY_PATTERN.matcher(toolOutput);
        List<Map.Entry<Path, Path>> res = new ArrayList<>();
        while (matcher.find()) {

            Path p1 = Paths.get(matcher.group(1));
            Path p2 = Paths.get(matcher.group(2));
            res.add(Map.entry(p1, p2));
        }
        return res;

    }

    @Override
    public String getLanguage() {
        return "javascript";
    }

    private String runTool(List<String> fileParam) throws IOException {
        List<String> command = new ArrayList<>();
        command.add("npx");
        command.add("madge");
        command.add("--dot");
        command.addAll(fileParam);

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuffer output = new StringBuffer();
        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(process.getInputStream()))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line)
                        .append("\n");
            }
        }
        return output.toString();
    }
}
