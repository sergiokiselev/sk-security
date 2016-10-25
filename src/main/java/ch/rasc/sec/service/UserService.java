package ch.rasc.sec.service;

import ch.rasc.sec.dto.AesKeyDto;
import ch.rasc.sec.dto.VerifyDto;
import org.hibernate.validator.constraints.NotBlank;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * User: NotePad.by
 * Date: 2/21/2016.
 */
public interface UserService {
    VerifyDto handleLogin(@NotBlank String login, @NotBlank String password, @NotBlank String sessionId) throws InvalidKeyException, NoSuchAlgorithmException, IOException;

    String verifyCode(String sessionId, String code) throws NoSuchAlgorithmException, InvalidKeyException, IOException;

    AesKeyDto getAesKey(String rsaKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException, NoSuchPaddingException;
}
