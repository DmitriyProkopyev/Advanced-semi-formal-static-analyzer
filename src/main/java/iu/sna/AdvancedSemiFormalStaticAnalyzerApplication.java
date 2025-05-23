// package iu.sna;

// import iu.sna.cli.validator.ValidationException;
// import iu.sna.infrastructure.Tree;
// import org.springframework.boot.SpringApplication;
// import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.springframework.shell.command.annotation.CommandScan;

// import java.nio.file.Path;
// import java.util.*;

// @CommandScan
// @SpringBootApplication
// public class AdvancedSemiFormalStaticAnalyzerApplication {
//     public static void main(String[] args) {
//         try {
//             SpringApplication.run(AdvancedSemiFormalStaticAnalyzerApplication.class, args);
//         } catch (ValidationException exception) {
//             System.out.println("[WARN]: " + logMessage(exception));
//         } catch (Exception exception) {
//             System.out.println("[ERROR]: " + logMessage(exception));
//         }
//     }

//     private static String logMessage(Exception exception) {
//         return exception.getClass().getSimpleName() +
//                 " - " +
//                 exception.getMessage() +
//                 " - " +
//                 Optional.ofNullable(exception.getCause())
//                         .map(Throwable::getClass)
//                         .map(Class::getSimpleName)
//                         .orElse("No Throwable passed") +
//                 "\n";
//     }
// }