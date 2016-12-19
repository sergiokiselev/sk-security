package ch.rasc.sec.service;

import ch.rasc.sec.dto.*;
import ch.rasc.sec.model.SessionAttributes;
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
    VerifyDto handleLogin(@NotBlank String login, @NotBlank String password, @NotBlank String sessionId) throws InvalidKeyException, NoSuchAlgorithmException, IOException, AuthenticationException, InvalidAlgorithmParameterException, NoSuchPaddingException;

    TotpSecretDto verifyCode(String sessionId, String code) throws NoSuchAlgorithmException, InvalidKeyException, IOException, AuthenticationException, InvalidAlgorithmParameterException, NoSuchPaddingException;

    AesKeyPartDto getAesKeyPart(LoginDto rsaKey) throws NoSuchAlgorithmException, InvalidKeyException, InvalidKeySpecException, IOException, NoSuchPaddingException;

    String verifyToken(TokenDto token) throws NoSuchAlgorithmException, InvalidKeyException, AuthenticationException;

    SessionAttributes getSessionAttributes(String sessionId);

    IVectorDto getIVector(AesKeyPartDto aeskp) throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, NoSuchPaddingException, IOException;
}
