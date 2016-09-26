package ch.rasc.sec;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Import({GoogleAuth.class})
@Configuration
public class TestGoogleAuthApplication {

}
