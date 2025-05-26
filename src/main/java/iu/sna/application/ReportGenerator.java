package iu.sna.application;

import iu.sna.application.llm_stages.ValidationBlock;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class ReportGenerator {
    private final String titlePrefix;
    private final String titlePostfix;
    private final String sectionSeparator;
    private final Path location;

    public ReportGenerator(String titlePrefix, String titlePostfix, String sectionSeparator, Path location) {
        this.titlePrefix = titlePrefix;
        this.titlePostfix = titlePostfix;
        this.sectionSeparator = sectionSeparator;
        this.location = location;
    }

    public File generateReport(String name, Map<ValidationBlock, String> content) throws IOException {
        /*
        var report = this.location.resolve(name.replace(':', '_'));
        var builder = new StringBuilder();

        for (var block : content.keySet()) {
            builder.append(this.titlePrefix);
            builder.append(block.name);
            builder.append(this.titlePostfix);
            builder.append(content.get(block));
            builder.append(this.sectionSeparator);
        }

        builder.append("End of the report.");
        Files.write(report, builder.toString().getBytes(StandardCharsets.UTF_8));
        return report.toFile();

         */

        var report = this.location.resolve(name.replace(':', '_'));
        var builder = new StringBuilder();

        var block = content.keySet().stream().findAny().get();
        builder.append(content.get(block));

        builder.append("\n\nEnd of the report.");
        Files.write(report, builder.toString().getBytes(StandardCharsets.UTF_8));
        return report.toFile();
    }
}
