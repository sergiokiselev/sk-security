package ch.rasc.sec.service.impl;

import ch.rasc.sec.dto.UserDto;
import ch.rasc.sec.model.User;
import ch.rasc.sec.repository.UserRepository;
import ch.rasc.sec.service.GoogleAuthenticatorService;
import ch.rasc.sec.service.MailService;
import ch.rasc.sec.service.UserService;
import org.apache.commons.codec.binary.Base32;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * @author Sergey Kiselev
 */
@Service
@Transactional
@Validated
public class UserServiceImpl implements UserService, UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MailService mailService;

    @Autowired
    private GoogleAuthenticatorService authenticatorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public boolean handleLogin(@NotBlank String login, @NotBlank String password) throws InvalidKeyException, NoSuchAlgorithmException {
        User user = userRepository.findByEmail(login);
        if (user == null) {
            throw new UsernameNotFoundException("User with login " + login + " was not found");
        }
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (matches) {
            SecureRandom random = new SecureRandom();
            byte bytes[] = new byte[20];
            random.nextBytes(bytes);
            long timestamp = System.currentTimeMillis() / 1000 / 30;
            long code = authenticatorService.getCode(bytes, timestamp);
            user.setSecret(new Base32().encodeAsString(bytes));
            mailService.send("sergio.kiselev509@gmail.com",
                    "sergio.kiselev509@gmail.com", "", String.valueOf(code));
            return true;
        }
        return false;
    }

    @Override
    public long findIdByEmail(@NotBlank String email) {
        User user = userRepository.findByEmail(email);
        return user.getId();
    }

    @Override
    public boolean verifyCode(long userId, String codeString) throws NoSuchAlgorithmException, InvalidKeyException {
        User user = userRepository.findOne(userId);
        long code = Long.valueOf(codeString);
        return authenticatorService.verifyCode(user.getSecret(), code, 1);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        return user;
    }
}
