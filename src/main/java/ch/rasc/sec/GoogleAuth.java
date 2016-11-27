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
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

@SpringBootApplication
@Import({SecurityConfig.class})
public class GoogleAuth extends SpringBootServletInitializer {
	public static SecretKey serverGoogleKey;
	public static IvParameterSpec ivectorGoogle;

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

	@Bean
	public BASE64Encoder getBase64Encoder() {
		return new BASE64Encoder();
	}

	@Bean
	public BASE64Decoder getBase64Decoder() {
		return new BASE64Decoder();
	}

}
