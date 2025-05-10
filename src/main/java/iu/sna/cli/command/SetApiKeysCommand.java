package iu.sna.cli.command;

import iu.sna.cli.CommandStringBuilder;
import iu.sna.cli.SetApiKeysType;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

@Command(command = "set-api-keys", description = "Set API keys")
@Configuration
public class SetApiKeysCommand {

    @Command
    public String setApiKeys(
            @Option(
                    longNames = "type",
                    shortNames = 't',
                    description = "type description"
            ) SetApiKeysType type,
            @Option(
                    longNames = "keys",
                    shortNames = 'k',
                    description = "keys description"
            ) String[] keys
    ) {
        return new CommandStringBuilder()
                .addObject(type)
                .addObject(keys)
                .toString();
    }
}
