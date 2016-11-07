package ch.rasc.sec.service;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * User: NotePad.by
 * Date: 2/25/2016.
 */
public interface TOTPService {
    byte[] generateSecret();

    boolean verifyCode(String secret, long code, int variance)
            throws InvalidKeyException, NoSuchAlgorithmException;

    long getCode(byte[] secret, long timeIndex)
            throws NoSuchAlgorithmException, InvalidKeyException;
}
