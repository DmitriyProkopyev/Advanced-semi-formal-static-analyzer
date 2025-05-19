// package iu.sna.cli.command;

// import iu.sna.cli.validator.Validator;
// import org.springframework.context.annotation.Configuration;
// import org.springframework.shell.command.annotation.Command;
// import org.springframework.shell.command.annotation.Option;

// import java.nio.file.Path;

// @Command(
//         command = "scan",
//         description = "Apply a git-warden profile setup to scan a specific git-repository and generate the compliance report"
// )
// @Configuration
// public class ScanCommand {

//     @Command
//     public String command(
//             @Option(
//                     longNames = "repository",
//                     shortNames = 'r',
//                     description = "The directory of the local git repository being analyzed",
//                     required = true
//             ) Path repository,
//             @Option(
//                     longNames = "profile",
//                     shortNames = 'p',
//                     description = "Name of the git-warden profile to use for analysis",
//                     required = true
//             ) String profile,
//             @Option(
//                     longNames = "output",
//                     shortNames = 'o',
//                     description = "Name and location for the generated compliance report",
//                     required = true
//             ) Path output
//     ) {
//         Validator.getInstance()
//                 .pathExists(repository)
//                 .pathExists(output)
//                 .isDirectory(repository)
//                 .gitFolderExists(repository)
//                 .isFile(output)
//                 .validate();

//         return repository + "\n" + profile + "\n" + output;
//     }
// }
