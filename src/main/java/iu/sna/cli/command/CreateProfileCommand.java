package iu.sna.cli.command;

import iu.sna.cli.CommandStringBuilder;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.io.File;

@Command(command = "create-profile", description = "Create profile")
@Configuration
public class CreateProfileCommand {

    @Command
    public String createProfile(
            @Option(
                    longNames = "name",
                    shortNames = 'n',
                    description = "name description"
            ) String name,
            @Option(
                    longNames = "stands",
                    shortNames = 's',
                    description = "standards description"
            ) File standardsFile,
            @Option(
                    longNames = "resp",
                    shortNames = 'r',
                    description = "responsibility areas description"
            ) File respAreasFile,
            @Option(
                    longNames = "api",
                    shortNames = 'a',
                    description = "api keys description"
            ) File apiKeysFile
    ) {
        return new CommandStringBuilder()
                .addObject(name)
                .addObject(standardsFile)
                .addObject(respAreasFile)
                .addObject(apiKeysFile)
                .toString();
    }
}
