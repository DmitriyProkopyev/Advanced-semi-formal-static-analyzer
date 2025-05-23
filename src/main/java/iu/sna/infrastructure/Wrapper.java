package iu.sna.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import iu.sna.application.Config;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Wrapper {
    private final String pythonExec;
    private final String scriptPath;
    private final ObjectMapper mapper = new ObjectMapper();

    public Wrapper() {
        try {
            File scriptFile = new File(Config.pythonWrapper.toString());
            File scriptDir = scriptFile.getParentFile();
            File venvDir = new File(scriptDir, "venv");

            if (!venvDir.exists()) {
                createVenv(venvDir);
            }

            installRequirements(venvDir, scriptDir);

            this.pythonExec = getVenvPythonPath(venvDir);
            this.scriptPath = Config.pythonWrapper.toString();
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to initialize Python environment", e);
        }
    }

    private void createVenv(File venvDir) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>();
        cmd.add(Config.python);
        cmd.add("-m");
        cmd.add("venv");
        cmd.add(venvDir.getAbsolutePath());

        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process process = pb.start();
        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Virtual environment creation failed with exit code " + exitCode);
        }
    }

    private void installRequirements(File venvDir, File scriptDir) throws IOException, InterruptedException {
        String venvPython = getVenvPythonPath(venvDir);
        if (!Files.exists(Config.pythonRequirements.toPath())) {
            throw new IOException("requirements.txt not found in " + scriptDir);
        }

        List<String> cmd = new ArrayList<>();
        cmd.add(venvPython);
        cmd.add("-m");
        cmd.add("pip");
        cmd.add("install");
        cmd.add("-r");
        cmd.add(Config.pythonRequirements.getAbsolutePath());

        var processBuilder = new ProcessBuilder(cmd);
        processBuilder.redirectErrorStream(true); // Combine stdout and stderr
        Process process = processBuilder.start();

        // Read the output to prevent buffer deadlock
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (Config.developerMode)
                    System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Requirements installation failed with exit code " + exitCode);
        }
    }

    private String getVenvPythonPath(File venvDir) {
        String osName = System.getProperty("os.name").toLowerCase();
        File pythonExec;
        if (osName.contains("win")) {
            pythonExec = new File(venvDir, "Scripts/python.exe");
        } else {
            pythonExec = new File(venvDir, "bin/python");
        }
        if (!pythonExec.exists()) {
            throw new RuntimeException("Python executable not found in venv: " + pythonExec.getAbsolutePath());
        }
        return pythonExec.getAbsolutePath();
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

    public List<Map<String, Object>> token(Collection<String> files) throws IOException, InterruptedException {
        var cmd = new ArrayList<String>();
        cmd.add(pythonExec);
        cmd.add(scriptPath);
        cmd.add("token");
        cmd.addAll(files);
        String out = runProcess(cmd);
        return mapper.readValue(out, new TypeReference<>() {});
    }

    public List<List<String>> cluster(String configJsonPath) throws IOException, InterruptedException {
        var cmd = List.of(pythonExec, scriptPath, "cluster", configJsonPath);
        String out = runProcess(cmd);
        return mapper.readValue(out, new TypeReference<>() {});
    }

    public boolean convert(String inputMd, String outputPdf) throws IOException, InterruptedException {
        var cmd = List.of(pythonExec, scriptPath, "convert", inputMd, outputPdf);
        String out = runProcess(cmd);
        Map<String, String> resp = mapper.readValue(out, new TypeReference<>() {});
        return "ok".equals(resp.get("status"));
    }
}
