package ch.rasc.sec.crypto;

import lombok.extern.log4j.Log4j;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * Created by Victoria on 23.10.2016.
 */

@Log4j
public class AES {
    private static final String cipherKind = "AES/OFB/PKCS5Padding";
    private static final int keyLength = 128;

    private static byte[] currentIV;

    public static byte[] getCurrentIV() {
        return currentIV;
    }

    public static SecretKey generateKey(){
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            log.error("Exception while generating AES key", e);
        }
        keyGen.init(keyLength);
        return keyGen.generateKey();
    }

    public static byte[] encrypt(byte[] data, SecretKey aesKey){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Cipher encryptCipher = Cipher.getInstance(cipherKind);
            encryptCipher.init(Cipher.ENCRYPT_MODE, aesKey);
            currentIV = encryptCipher.getIV();
            CipherOutputStream cipherOutputStream = new CipherOutputStream(outputStream, encryptCipher);
            cipherOutputStream.write(data);
            cipherOutputStream.flush();
            cipherOutputStream.close();
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IOException e) {
            log.error("Exception while encrypting data with AES", e);
        }
        return outputStream.toByteArray();
    }

    public static byte[] decrypt(byte[] data, SecretKey aesKey, IvParameterSpec ivParameterSpec){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Cipher decryptCipher = Cipher.getInstance(cipherKind);
            decryptCipher.init(Cipher.DECRYPT_MODE, aesKey, ivParameterSpec);
            ByteArrayInputStream inStream = new ByteArrayInputStream(data);
            CipherInputStream cipherInputStream = new CipherInputStream(inStream, decryptCipher);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = cipherInputStream.read(buf)) >= 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            cipherInputStream.close();
        } catch (InvalidKeyException | InvalidAlgorithmParameterException |
                NoSuchAlgorithmException | NoSuchPaddingException | IOException e) {
            log.error("Exception while decrypting AES-encrypted data", e);
        }
        return outputStream.toByteArray();
    }
}
