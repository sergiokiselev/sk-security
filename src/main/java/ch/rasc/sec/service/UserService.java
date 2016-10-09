package ch.rasc.sec.service;

import ch.rasc.sec.dto.UserDto;
import ch.rasc.sec.dto.VerifyDto;
import org.hibernate.validator.constraints.NotBlank;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * User: NotePad.by
 * Date: 2/21/2016.
 */
public interface UserService {
    VerifyDto handleLogin(@NotBlank String login, @NotBlank String password) throws InvalidKeyException, NoSuchAlgorithmException;

    String verifyCode(String sessionId, String code) throws NoSuchAlgorithmException, InvalidKeyException;
}
