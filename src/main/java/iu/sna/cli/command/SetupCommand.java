package iu.sna.cli.command;

import iu.sna.cli.config.CommandUtils;
import iu.sna.cli.config.SetupType;
import iu.sna.cli.validator.Validator;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Command(
        command = "setup",
        description = "Install and setup the tool, required to operate properly"
)
@Component
public class SetupCommand {

    @Command
    public String command(
            @Option(
                    longNames = "type",
                    shortNames = 't',
                    description = "LLM the API of which will be used for semi-formal and non-formal tasks execution",
                    required = true
            ) SetupType type,
            @Option(
                    longNames = "keys",
                    shortNames = 'k',
                    description = "A list of strings representing LLM provider API keys"
            ) String[] keys,
            @Option(
                    longNames = "file-keys",
                    shortNames = 'f',
                    description = "Path to a file containing an LLM provider API key on its every line"
            ) Path fileKeys
    ) throws IOException {
        Validator.getInstance()
                .pathExists(fileKeys)
                .isFile(fileKeys)
                .eitherParamOrFileApiKeys(keys, fileKeys)
                .validate();

        CommandUtils.setSetupType(type);
        CommandUtils.setApiKeys(keys, fileKeys);
        return "Setup completed successfully for " + type.name();
    }
}
