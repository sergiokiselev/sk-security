package ch.rasc.sec.secureserver;

import ch.rasc.sec.secureserver.config.security.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

@SpringBootApplication

public class StorageBoot extends SpringBootServletInitializer {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(StorageBoot.class, args);
    }


    @Bean
    public IdGenerator getGenerator() {
        return new JdkIdGenerator();
    }

}
