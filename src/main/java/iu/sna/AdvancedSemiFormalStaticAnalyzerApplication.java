package iu.sna;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.shell.command.annotation.CommandScan;

@CommandScan
@SpringBootApplication
public class AdvancedSemiFormalStaticAnalyzerApplication {
    public static void main(String[] args) {
        SpringApplication.run(AdvancedSemiFormalStaticAnalyzerApplication.class, args);
    }
}
