package iu.sna.cli.command;

import iu.sna.cli.CommandStringBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.File;

@Command(command = "apply-profile", description = "Apply profile")
@Configuration
public class ApplyProfileCommand {

    @Command
    public String applyProfile(
            @Option(
                    longNames = "name",
                    shortNames = 'n',
                    description = "name description"
            ) String name,
            @Option(
                    longNames = "stands",
                    shortNames = 's',
                    description = "standards description"
            ) File repositoryPath
    ) {
        return new CommandStringBuilder()
                .addObject(name)
                .addObject(repositoryPath)
                .toString();
    }
}
