package ch.rasc.sec.config.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({WebSecurityConfig.class})
public class SecurityConfig {
}
