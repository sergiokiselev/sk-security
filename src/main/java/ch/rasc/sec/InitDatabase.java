package ch.rasc.sec;

import ch.rasc.sec.model.User;
import ch.rasc.sec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InitDatabase implements ApplicationListener<ContextRefreshedEvent> {

	private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

	@Autowired
	public InitDatabase(PasswordEncoder passwordEncoder, UserRepository userRepository) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	@Transactional
	public void onApplicationEvent(ContextRefreshedEvent event) {
		if (this.userRepository.count() == 0) {
			User adminUser = new User();
			adminUser.setEmail("superadmin@gmail.com");
			adminUser.setSecret("IB6EFEQKE7U2TQIB");
			adminUser.setPassword(passwordEncoder.encode("password"));
			userRepository.save(adminUser);
			User seorgy = new User("sergio.kiselev509@gmail.com", passwordEncoder.encode("password"), null);
			userRepository.save(seorgy);
			User denis = new User("m-den-i@yandex.by", passwordEncoder.encode("password"), null);
			userRepository.save(denis);
		}

	}

}
