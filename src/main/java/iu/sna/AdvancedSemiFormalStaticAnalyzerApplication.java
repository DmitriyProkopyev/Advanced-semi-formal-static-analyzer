package iu.sna;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class AdvancedSemiFormalStaticAnalyzerApplication {
    @Value("${constants.commit_importance_coefficient}")
    private double s;
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AdvancedSemiFormalStaticAnalyzerApplication.class, args);

        // Получаем бин и печатаем значение
        AdvancedSemiFormalStaticAnalyzerApplication app = context.getBean(AdvancedSemiFormalStaticAnalyzerApplication.class);
        System.out.println(app.s);  }
}
