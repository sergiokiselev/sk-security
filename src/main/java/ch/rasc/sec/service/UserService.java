package ch.rasc.sec.service;

import ch.rasc.sec.dto.UserDto;
import org.hibernate.validator.constraints.NotBlank;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * User: NotePad.by
 * Date: 2/21/2016.
 */
public interface UserService {
    boolean handleLogin(@NotBlank String login, @NotBlank String password) throws InvalidKeyException, NoSuchAlgorithmException;

    long findIdByEmail(@NotBlank String email);

    boolean verifyCode(long userId, String code) throws NoSuchAlgorithmException, InvalidKeyException;
}
