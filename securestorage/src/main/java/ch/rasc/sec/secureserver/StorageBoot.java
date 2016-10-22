package ch.rasc.sec.secureserver;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class StorageBoot extends SpringBootServletInitializer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(StorageBoot.class, args);
    }
}
