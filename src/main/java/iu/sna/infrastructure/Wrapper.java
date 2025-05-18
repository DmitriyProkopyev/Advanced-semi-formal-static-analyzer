package iu.sna.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class Wrapper {
    private final String pythonExec;
    private final String scriptPath;
    private final ObjectMapper mapper = new ObjectMapper();

    public Wrapper(String pythonExec, String scriptPath) {
        this.pythonExec = pythonExec;
        this.scriptPath = scriptPath;
    }

    private String runProcess(List<String> cmd) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(proc.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            if (proc.waitFor() != 0) {
                throw new RuntimeException("Exit code != 0: " + sb);
            }
            return sb.toString();
        }
    }

    public List<Map<String, Object>> token(List<String> files) throws IOException, InterruptedException {
        var cmd = new java.util.ArrayList<String>();
        cmd.add(pythonExec);
        cmd.add(scriptPath);
        cmd.add("token");
        cmd.addAll(files);
        String out = runProcess(cmd);
        return mapper.readValue(out, new TypeReference<>() {
        });
    }

    public List<List<String>> cluster(String configJsonPath) throws IOException, InterruptedException {
        var cmd = List.of(pythonExec, scriptPath, "cluster", configJsonPath);
        String out = runProcess(cmd);
        return mapper.readValue(out, new TypeReference<>() {
        });
    }

    public boolean convert(String inputMd, String outputPdf) throws IOException, InterruptedException {
        var cmd = List.of(pythonExec, scriptPath, "convert", inputMd, outputPdf);
        String out = runProcess(cmd);
        Map<String, String> resp = mapper.readValue(out, new TypeReference<>() {
        });
        return "ok".equals(resp.get("status"));
    }
}
