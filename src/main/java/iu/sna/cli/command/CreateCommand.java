package iu.sna.cli.command;

import iu.sna.cli.config.CommandUtils;
import iu.sna.cli.validator.Validator;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

@Command(
        command = "create",
        description = "Create a git-warden profile representing a validation setup for a specific git-repository"
)
@Component
public class CreateCommand {

    @Command
    public String command(
            @Option(
                    longNames = "name",
                    shortNames = 'n',
                    description = "User-defined string identifier of a git-warden profile"
            ) String name,
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
                    longNames = "context",
                    shortNames = 'c',
                    description = "File or directory with files describing the project requirements, stack, etc."
            ) Path[] context,
            @Option(
                    longNames = "priorities",
                    shortNames = 'p',
                    description = "Core requirements with ordinals to insert in necessary order",
                    required = true
            ) Integer[] priorities
    ) throws IOException {
        Validator.getInstance()
                .pathExists(repository)
                .pathExists(context)
                .isDirectory(repository)
                .notEqualPaths(repository, context)
                .gitFolderExists(repository)
                .isValidFileType(context)
                .validate();

        if (name == null) {
            name = repository.toFile().getCanonicalFile().getName();
        }

        return String.join("\n", Objects.requireNonNull(
                CommandUtils.createApplicationFacade(
                                CommandUtils.getSetupType(),
                                CommandUtils.getApiKeys()
                        ).createProfile(
                                name,
                                context == null ? new ArrayList<>() : Arrays.asList(context),
                                CommandUtils.createDirectoryTree(repository, directories),
                                Arrays.asList(priorities)
                        )));
    }
}
