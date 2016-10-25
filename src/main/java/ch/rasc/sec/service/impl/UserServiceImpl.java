package ch.rasc.sec.service.impl;

import ch.rasc.sec.cypher.AES;
import ch.rasc.sec.cypher.RSA;
import ch.rasc.sec.dto.AesKeyDto;
import ch.rasc.sec.model.SessionAttributes;
import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.dto.TotpSecretDto;
import ch.rasc.sec.dto.VerifyDto;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
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
    public VerifyDto handleLogin(@NotBlank String login, @NotBlank String password, @NotBlank String sessionId) throws InvalidKeyException, NoSuchAlgorithmException, IOException, AuthenticationException {
        SessionAttributes sessionAttributes = this.sessionAttributes.get(sessionId);
        SecretKey secretKey = sessionAttributes.getAesKey();
        login = new String(AES.decrypt(decoder.decodeBuffer(login), secretKey, new IvParameterSpec(sessionAttributes.getIvector())));
        password = new String(AES.decrypt(decoder.decodeBuffer(password), secretKey, new IvParameterSpec(sessionAttributes.getIvector())));
        User user = userRepository.findByEmail(login);
        if (user == null) {
            throw new UsernameNotFoundException("User with login " + login + " was not found");
        }
        VerifyDto verifyDto = new VerifyDto();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthenticationException("Invalid password");
        }
        sessionAttributes.setCorrectPassword(true);
        generateAndSendCode(user, sessionAttributes);
        verifyDto.setSessionId(sessionId);
        return verifyDto;
    }

    private void generateAndSendCode(User user, SessionAttributes sessionAttributes) throws NoSuchAlgorithmException, InvalidKeyException {
        SecureRandom random = new SecureRandom();
        int code = Math.abs(random.nextInt());
        sessionAttributes.setCode(String.valueOf(code));
        mailService.send("specialcourse665@gmail.com", user.getEmail(), "", String.valueOf(code));
    }

    @Override
    public TotpSecretDto verifyCode(String sessionId, String codeString) throws NoSuchAlgorithmException, InvalidKeyException, IOException, AuthenticationException, InvalidAlgorithmParameterException, NoSuchPaddingException {
        SessionAttributes sessionAttributes = this.sessionAttributes.get(sessionId);
        if (!sessionAttributes.isCorrectPassword()) {
            throw new AuthenticationException("You haven't entered correct password");
        }
        SecretKey secretKey = sessionAttributes.getAesKey();
        codeString = new String(AES.decrypt(decoder.decodeBuffer(codeString),secretKey, new IvParameterSpec(sessionAttributes.getIvector())));
        if (!sessionAttributes.getCode().equals(codeString)) {
            throw new AuthenticationException("Invalid code");
        }
        byte[] secret = authenticatorService.generateSecret();
        sessionAttributes.setTotpSecret(secret);
        String encodedSecret = encoder.encode(AES.encrypt(secret, secretKey, new IvParameterSpec(sessionAttributes.getIvector())));
        return new TotpSecretDto(encodedSecret, null, sessionId);
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
    public AesKeyDto getAesKey(String rsaKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException, NoSuchPaddingException {
        SecretKey secretKey = AES.generateKey();
        byte[] decodedKey = decoder.decodeBuffer(rsaKey);
        PublicKey publicKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(decodedKey));
        AesKeyDto aesKD = new AesKeyDto();
        UUID uuid = generator.generateId();
        aesKD.setAesKey(encoder.encode(RSA.encrypt(secretKey.getEncoded(), publicKey)));
        aesKD.setSessionId(uuid.toString());
        byte[] ivector = AES.generateIV(secretKey);
        aesKD.setIvector(encoder.encode(RSA.encrypt(ivector, publicKey)));
        if(sessionAttributes==null)
            sessionAttributes = new HashMap<>();
        sessionAttributes.put(aesKD.getSessionId(), new SessionAttributes(secretKey, ivector));
        return aesKD;
    }

    @Override
    public String verifyToken(TokenDto token) throws NoSuchAlgorithmException, InvalidKeyException, AuthenticationException {
        SessionAttributes sessionAttributes = this.sessionAttributes.get(token.getSessionId());
        long tokenValue = token.getToken();
        byte[] totpSecret = sessionAttributes.getTotpSecret();
        if (!authenticatorService.verifyCode(new Base32().encodeAsString(totpSecret), tokenValue, 1)) {
            throw new AuthenticationException("Invalid token");
        }
        return "Yeah!!! You did it!!!";
    }
}
