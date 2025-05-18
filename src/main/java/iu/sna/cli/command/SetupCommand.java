package iu.sna.cli.command;

import iu.sna.cli.SetupType;
import iu.sna.cli.validator.Validator;
import org.springframework.context.annotation.Configuration;
import org.springframework.shell.command.annotation.Command;
import org.springframework.shell.command.annotation.Option;

import java.nio.file.Path;
import java.util.Arrays;

@Command(
        command = "setup",
        description = "Install and setup the tool, required to operate properly"
)
@Configuration
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
    ) {
        Validator.getInstance()
                .pathExists(fileKeys)
                .isFile(fileKeys)
                .eitherParamOrFileApiKeys(keys, fileKeys)
                .validate();

        return type.name() + "\n" + Arrays.toString(keys) + "\n" + fileKeys;
    }
}
