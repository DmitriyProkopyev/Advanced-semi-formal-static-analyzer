// package iu.sna.cli.command;

// import iu.sna.cli.validator.Validator;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.shell.command.annotation.Command;
// import org.springframework.shell.command.annotation.Option;

// import java.io.IOException;
// import java.nio.file.Path;

// @Command(
//         command = "create",
//         description = "Create a git-warden profile representing a validation setup for a specific git-repository"
// )
// @Configuration
// public class CreateCommand {

//     @Command
//     public String command(
//             @Option(
//                     longNames = "name",
//                     shortNames = 'n',
//                     description = "User-defined string identifier of a git-warden profile"
//             ) String name,
//             @Option(
//                     longNames = "repository",
//                     shortNames = 'r',
//                     description = "The directory of the local git repository being analyzed",
//                     required = true
//             ) Path repository,
//             @Option(
//                     longNames = "context",
//                     shortNames = 'c',
//                     description = "file or directory with files describing the project requirements, stack, etc."
//             ) Path context
//     ) throws IOException {
//         Validator.getInstance()
//                 .pathExists(repository)
//                 .pathExists(context)
//                 .isDirectory(repository)
//                 .notEqualPaths(repository, context)
//                 .gitFolderExists(repository)
//                 .isValidFileType(context)
//                 .validate();

//         if (name == null) {
//             name = repository.toFile().getCanonicalFile().getName();
//         }

//         return name + "\n" + repository + "\n" + context;
//     }
// }
