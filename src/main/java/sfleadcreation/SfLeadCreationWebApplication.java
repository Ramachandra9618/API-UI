package sfleadcreation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"sfleadcreation", "controller"})
public class SfLeadCreationWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(SfLeadCreationWebApplication.class, args);
    }
}
