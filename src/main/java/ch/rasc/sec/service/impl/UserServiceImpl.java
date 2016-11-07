package ch.rasc.sec.service.impl;

import ch.rasc.sec.cypher.AES;
import ch.rasc.sec.cypher.RSA;
import ch.rasc.sec.dto.*;
import ch.rasc.sec.model.SessionAttributes;
import ch.rasc.sec.model.User;
import ch.rasc.sec.repository.UserRepository;
import ch.rasc.sec.service.MailService;
import ch.rasc.sec.service.TOTPService;
import ch.rasc.sec.service.UserService;
import ch.rasc.sec.util.exception.AuthenticationException;
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
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
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

    private Map<String, SessionAttributes> sessionAttributes;

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

    @Autowired
    private BASE64Encoder encoder;

    @Autowired
    private BASE64Decoder decoder;

    @Override
    public VerifyDto handleLogin(@NotBlank String login, @NotBlank String password, @NotBlank String sessionId) throws InvalidKeyException, NoSuchAlgorithmException, IOException, AuthenticationException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        SessionAttributes sessionAttributes = this.sessionAttributes.get(sessionId);
        SecretKey secretKey = sessionAttributes.getAesKey();
        byte[] loginBytes = decoder.decodeBuffer(login);
        byte[] passwordBytes = decoder.decodeBuffer(password);
        boolean encrypted = sessionAttributes.isEncryption();
        if (encrypted) {
            login = new String(AES.decrypt(loginBytes, secretKey, new IvParameterSpec(sessionAttributes.getIvector())));
            password = new String(AES.decrypt(passwordBytes, secretKey, new IvParameterSpec(sessionAttributes.getIvector())));
        } else {
            login = new String(loginBytes);
            password = new String(passwordBytes);
        }
        System.out.println(login);
        System.out.println(password);
        User user = userRepository.findByEmail(login);
        if (user == null) {
            throw new UsernameNotFoundException("User with login " + login + " was not found");
        }
        if (!user.isAccountNonLocked())
            throw new AuthenticationException("Account is locked, you should contact administrator.");
        VerifyDto verifyDto = new VerifyDto();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            user.setTries(user.getTries() + 1);
            throw new AuthenticationException("Invalid password");
        }
        sessionAttributes.setCorrectPassword(true);
        user.setTries(0);
        System.out.println("Is post code: " + sessionAttributes.isPostCode());
        if (sessionAttributes.isPostCode()) {
            generateAndSendCode(user, sessionAttributes);
        } else {
            String secretString = getSecretString(sessionAttributes, secretKey);
            verifyDto.setSecret(secretString);
        }
        verifyDto.setSessionId(sessionId);
        return verifyDto;
    }

    private String getSecretString(SessionAttributes sessionAttributes, SecretKey secretKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        byte[] secret = authenticatorService.generateSecret();
        sessionAttributes.setTotpSecret(secret);
        String secretString;
        if (sessionAttributes.isEncryption()) {
            secretString = encoder.encode(AES.encrypt(secret, secretKey, new IvParameterSpec(sessionAttributes.getIvector())));
        } else {
            secretString = encoder.encode(secret);
        }
        return secretString;
    }

    private void generateAndSendCode(User user, SessionAttributes sessionAttributes) throws NoSuchAlgorithmException, InvalidKeyException {
        SecureRandom random = new SecureRandom();
        int code = Math.abs(random.nextInt());
        sessionAttributes.setCode(String.valueOf(code));
        System.out.println("User: " + user.getEmail());
        System.out.println("Code: " + code);
        mailService.send("specialcourse665@gmail.com", user.getEmail(), "", String.valueOf(code));
    }

    @Override
    public TotpSecretDto verifyCode(String sessionId, String codeString) throws NoSuchAlgorithmException, InvalidKeyException, IOException, AuthenticationException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        SessionAttributes sessionAttributes = this.sessionAttributes.get(sessionId);
        if (!sessionAttributes.isCorrectPassword()) {
            throw new AuthenticationException("You haven't entered correct password");
        }
        SecretKey secretKey = sessionAttributes.getAesKey();
        if (sessionAttributes.isEncryption()) {
            codeString = new String(AES.decrypt(decoder.decodeBuffer(codeString), secretKey, new IvParameterSpec(sessionAttributes.getIvector())));
        } else {
            codeString = new String(decoder.decodeBuffer(codeString));
        }
        if (!sessionAttributes.getCode().equals(codeString)) {
            throw new AuthenticationException("Invalid code");
        }
        String secretString = getSecretString(sessionAttributes, secretKey);
        return new TotpSecretDto(secretString, sessionId);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        return user;
    }

    private SessionAttributes getNewSessionAttributes() {
        if(sessionAttributes==null) {
            sessionAttributes = new HashMap<>();
        }
        UUID uuid = generator.generateId();
        String sessionId = uuid.toString();
        sessionAttributes.put(sessionId, new SessionAttributes(sessionId));
        return sessionAttributes.get(sessionId);
    }

    @Override
    public AesKeyDto getAesKey(RsaKeyDto rsaKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException, NoSuchPaddingException {
        SecretKey secretKey = AES.generateKey();
        boolean encryption = rsaKey.isEncryption();
        boolean postCode = rsaKey.isPostCode();
        SessionAttributes attributes = getNewSessionAttributes();
        attributes.setPostCode(postCode);
        attributes.setEncryption(encryption);
        AesKeyDto aesKD = new AesKeyDto();
        aesKD.setSessionId(attributes.getSessionId());
        if (encryption) {
            byte[] decodedKey = decoder.decodeBuffer(rsaKey.getRsaKey());
            PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));
            aesKD.setAesKey(encoder.encode(RSA.encrypt(secretKey.getEncoded(), publicKey)));
            byte[] ivector = AES.generateIV(secretKey);
            aesKD.setIvector(encoder.encode(RSA.encrypt(ivector, publicKey)));
            attributes.setIvector(ivector);
            attributes.setAesKey(secretKey);
        }
        return aesKD;
    }

    @Override
    public String verifyToken(TokenDto token) throws NoSuchAlgorithmException, InvalidKeyException, AuthenticationException {
        SessionAttributes sessionAttributes = this.sessionAttributes.get(token.getSessionId());
        System.out.println("Sesstion attributes: " + sessionAttributes);
        long tokenValue = token.getToken();
        byte[] totpSecret = sessionAttributes.getTotpSecret();
        if (!authenticatorService.verifyCode(new Base32().encodeAsString(totpSecret), tokenValue, 1)) {
            throw new AuthenticationException("Invalid token");
        }
        return "Yeah!!! You did it!!!";
    }
}
