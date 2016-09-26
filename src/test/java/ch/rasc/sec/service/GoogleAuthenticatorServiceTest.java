package ch.rasc.sec.service;

import ch.rasc.sec.GenericTest;
import ch.rasc.sec.service.impl.GoogleAuthenticatorServiceImpl;
import org.apache.commons.codec.binary.Base32;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.boot.test.IntegrationTest;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static org.junit.Assert.*;

/**
 * User: NotePad.by
 * Date: 2/25/2016.
 */
@IntegrationTest
public class GoogleAuthenticatorServiceTest extends GenericTest {


    private GoogleAuthenticatorService authenticatorService = new GoogleAuthenticatorServiceImpl();

    @Test
    public void testCodeSize() throws InvalidKeyException, NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        long code = authenticatorService.getCode(bytes, 30L);
        assertEquals(6, String.valueOf(code).length());
    }

    @Test
    public void testVerifyCode() throws InvalidKeyException, NoSuchAlgorithmException {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        long code = authenticatorService.getCode(bytes, timeIndex);
        boolean verify = authenticatorService.verifyCode(
                new Base32().encodeAsString(bytes), code, 1);
        assertTrue(verify);
    }

    @Test
    @Ignore
    public void testVerificationFails() throws NoSuchAlgorithmException, InvalidKeyException, InterruptedException {
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[20];
        random.nextBytes(bytes);
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        long code = authenticatorService.getCode(bytes, timeIndex);
        Thread.sleep(31000);
        boolean verify = authenticatorService.verifyCode(
                new Base32().encodeAsString(bytes), code, 1);
        assertFalse(verify);
    }

}
