package ch.rasc.sec.crypto;

import lombok.extern.log4j.Log4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;

/**
 * Created by Victoria on 23.10.2016.
 */

@Log4j
public class RSA {
    public static byte[] decrypt(byte[] data, PrivateKey privateKey) {
        Cipher cipher = null;
        try {
            cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        byte[] cipherData = null;
        try {
            cipherData = cipher.doFinal(data);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            log.error("Exception while decrypting RSA-encrypted data", e);
        }
        return cipherData;
    }
}
