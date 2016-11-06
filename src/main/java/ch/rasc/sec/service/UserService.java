package ch.rasc.sec.service;

import ch.rasc.sec.dto.AesKeyDto;
import ch.rasc.sec.dto.TokenDto;
import ch.rasc.sec.dto.TotpSecretDto;
import ch.rasc.sec.dto.VerifyDto;
import ch.rasc.sec.util.exception.AuthenticationException;
import org.hibernate.validator.constraints.NotBlank;

import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * User: NotePad.by
 * Date: 2/21/2016.
 */
public interface UserService {
    VerifyDto handleLogin(@NotBlank String login, @NotBlank String password, @NotBlank String sessionId) throws InvalidKeyException, NoSuchAlgorithmException, IOException, AuthenticationException;

    TotpSecretDto verifyCode(String sessionId, String code) throws NoSuchAlgorithmException, InvalidKeyException, IOException, AuthenticationException, InvalidAlgorithmParameterException, NoSuchPaddingException;

    AesKeyDto getAesKey(String rsaKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException, NoSuchPaddingException;

    String verifyToken(TokenDto token) throws NoSuchAlgorithmException, InvalidKeyException, AuthenticationException;
}
