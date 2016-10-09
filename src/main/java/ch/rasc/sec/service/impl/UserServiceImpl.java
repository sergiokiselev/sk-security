package ch.rasc.sec.service.impl;

import ch.rasc.sec.dto.VerifyDto;
import ch.rasc.sec.model.User;
import ch.rasc.sec.repository.UserRepository;
import ch.rasc.sec.service.MailService;
import ch.rasc.sec.service.TOTPService;
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
import org.springframework.util.IdGenerator;
import org.springframework.validation.annotation.Validated;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;

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
    private TOTPService authenticatorService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private IdGenerator generator;

    @Override
    public VerifyDto handleLogin(@NotBlank String login, @NotBlank String password) throws InvalidKeyException, NoSuchAlgorithmException {
        User user = userRepository.findByEmail(login);
        if (user == null) {
            throw new UsernameNotFoundException("User with login " + login + " was not found");
        }
        VerifyDto verifyDto = new VerifyDto();
        if (passwordEncoder.matches(password, user.getPassword())) {
            byte[] bytes = generateAndSendCode(user);
            UUID uuid = generator.generateId();
            verifyDto.setSessionId(uuid.toString());
            user.setSecret(new Base32().encodeAsString(bytes));
            user.setAuthSessionId(uuid.toString());
            return verifyDto;
        } else {
            verifyDto.setException("wrong password");
        }
        return verifyDto;
    }

    private byte[] generateAndSendCode(User user) throws NoSuchAlgorithmException, InvalidKeyException {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        long timestamp = System.currentTimeMillis() / 1000 / 30;
        long code = authenticatorService.getCode(bytes, timestamp);
        mailService.send("specialcourse665@gmail.com", user.getEmail(), "", String.valueOf(code));
        return bytes;
    }

    @Override
    public String verifyCode(String sessionId, String codeString) throws NoSuchAlgorithmException, InvalidKeyException {
        User user = userRepository.findByAuthSessionId(sessionId);
        long code = Long.valueOf(codeString);
        System.out.println("code " + code);
        return String.valueOf(authenticatorService.verifyCode(user.getSecret(), code, 2));
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
