import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class MdToPdfConverter {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java MdToPdfConverter <input.md> [output.pdf]");
            System.exit(1);
        }

        String inputFilePath = args[0];
        String outputFilePath = (args.length >= 2) ? args[1] : "output.pdf";

        convertMdToPdf(inputFilePath, outputFilePath);
    }

    public static void convertMdToPdf(String inputFilePath, String outputFilePath) {
        File inputFile = new File(inputFilePath);
        if (!inputFile.exists()) {
            System.err.println("Error: Input file '" + inputFilePath + "' not found!");
            System.exit(1);
        }

        String[] command = {
                "pandoc",
                inputFilePath,
                "-o", outputFilePath,
                "--pdf-engine=xelatex",
                "--listings"
        };

        try {
            System.out.println("Converting '" + inputFilePath + "' to '" + outputFilePath + "'...");

            Process process = Runtime.getRuntime().exec(command);

            BufferedReader errorReader = new BufferedReader(
                    new InputStreamReader(process.getErrorStream())
            );

            String line;
            while ((line = errorReader.readLine()) != null) {
                System.err.println("Pandoc: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Conversion successful!");
            } else {
                System.err.println("Error: Pandoc exited with code " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.err.println("Error during conversion:");
            e.printStackTrace();
        }
    }
}