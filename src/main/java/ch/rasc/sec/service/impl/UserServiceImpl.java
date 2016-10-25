package ch.rasc.sec.service.impl;

import ch.rasc.sec.cypher.AES;
import ch.rasc.sec.cypher.RSA;
import ch.rasc.sec.dto.AesKeyDto;
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

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Sergey Kiselev
 */
@Service
@Transactional
@Validated
public class UserServiceImpl implements UserService, UserDetailsService {

    private Map<String,SecretKey> sessionAesMap;

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
    public VerifyDto handleLogin(@NotBlank String login, @NotBlank String password, @NotBlank String sessionId) throws InvalidKeyException, NoSuchAlgorithmException {
        SecretKey secretKey = sessionAesMap.get(sessionId);
        login = new String(AES.decrypt(login.getBytes(),secretKey, new IvParameterSpec(AES.getCurrentIV())));
        password = new String(AES.decrypt(password.getBytes(),secretKey, new IvParameterSpec(AES.getCurrentIV())));
        User user = userRepository.findByEmail(login);
        if (user == null) {
            throw new UsernameNotFoundException("User with login " + login + " was not found");
        }
        VerifyDto verifyDto = new VerifyDto();
        if (passwordEncoder.matches(password, user.getPassword())) {
            byte[] bytes = generateAndSendCode(user);
           // UUID uuid = generator.generateId();
            verifyDto.setSessionId(sessionId);
            user.setSecret(new Base32().encodeAsString(bytes));
            user.setAuthSessionId(sessionId);
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
        SecretKey secretKey = sessionAesMap.get(sessionId);
        codeString = new String(AES.decrypt(codeString.getBytes(),secretKey, new IvParameterSpec(AES.getCurrentIV())));
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

    @Override
    public AesKeyDto setRsa(String rsaKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException {
        SecretKey secretKey = AES.generateKey();
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(rsaKey.getBytes()));
        AesKeyDto aesKD = new AesKeyDto();
        UUID uuid = generator.generateId();
        aesKD.setAesKey(new String(RSA.encrypt(secretKey.getEncoded(),publicKey)));
        aesKD.setSessionId(uuid.toString());
        aesKD.setIvector(new String(AES.getCurrentIV()));
        if(sessionAesMap==null)
            sessionAesMap = new HashMap<>();
        sessionAesMap.put(aesKD.getSessionId(), secretKey);
        return aesKD;

    }
}
