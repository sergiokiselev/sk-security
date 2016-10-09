package ch.rasc.sec;

import ch.rasc.sec.config.security.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.web.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;
import org.springframework.util.SimpleIdGenerator;

@SpringBootApplication
@Import({SecurityConfig.class})
public class GoogleAuth extends SpringBootServletInitializer {

	public static void main(String[] args) throws Exception {
		SpringApplication.run(GoogleAuth.class, args);
	}

	@Bean
	public PasswordEncoder getEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public IdGenerator getGenerator() {
		return new JdkIdGenerator();
	}

}
