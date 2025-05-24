package iu.sna.cli.command;

import iu.sna.cli.config.CommandUtils;
import iu.sna.cli.validator.Validator;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Objects;

@Command(
        command = "scan",
        description = "Apply a git-warden profile setup to scan a specific git-repository and generate the compliance report"
)
@Component
public class ScanCommand {

    @Command
    public String command(
            @Option(
                    longNames = "repository",
                    shortNames = 'r',
                    description = "The directory of the local git repository being analyzed",
                    required = true
            ) Path repository,
            @Option(
                    longNames = "directories",
                    shortNames = 'd',
                    description = "The filtered directories inside the repository being analyzed"
            ) Path[] directories,
            @Option(
                    longNames = "profile",
                    shortNames = 'p',
                    description = "Name of the git-warden profile to use for analysis",
                    required = true
            ) String profile,
            @Option(
                    longNames = "output",
                    shortNames = 'o',
                    description = "Name and location for the generated compliance report",
                    required = true
            ) Path output
    ) throws IOException {
        Validator.getInstance()
                .pathExists(repository)
                .pathExists(output)
                .isDirectory(repository)
                .gitFolderExists(repository)
                .isFile(output)
                .validate();

        return String.join("\n", Objects.requireNonNull(
                CommandUtils.createApplicationFacade(
                                CommandUtils.getSetupType(),
                                CommandUtils.getApiKeys()
                        ).scan(
                                profile,
                                CommandUtils.createDirectoryTree(repository, directories),
                                output.toFile()
                        )));
    }
}
