package ch.rasc.sec;

/**
 * Created by Victoria on 07.11.2016.
 */

import ch.rasc.sec.model.User;
import ch.rasc.sec.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class Registration  implements CommandLineRunner {
	private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;

	@Autowired
	public Registration(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... strings) throws Exception {
		boolean more = true;
		String email, password, answer;
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		while(more){
			System.out.println("Enter email:");
			email = br.readLine();
			System.out.println("Enter password:");
			password = br.readLine();
			User user = new User(email, passwordEncoder.encode(password), null, 0);
			userRepository.save(user);
			answer = "";
			while(!answer.equals("y") && more){
				System.out.println("Add more users? (y/n)");
				answer = br.readLine();
				if(answer.equals("n")){
					more = false;
				}
			}
		}
		br.close();
	}
}
