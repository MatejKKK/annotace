package cz.cvut.kbss.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"cz.cvut.kbss"})
public class LemmatizerApp {
    public static void main(String[] args){
        SpringApplication.run(LemmatizerApp.class);
    }
}
