package ch.rasc.sec;

import ch.rasc.sec.cypher.AES;
import ch.rasc.sec.cypher.RSA;
import ch.rasc.sec.dto.AesKeyDto;
import ch.rasc.sec.dto.UserDto;
import ch.rasc.sec.dto.VerifyDto;
import org.springframework.web.client.RestTemplate;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class TestGoogleAuthApplication {

    public static void main(String[] args) throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        RestTemplate restTemplate = new RestTemplate();
        KeyPair keyPair = RSA.generateKeyPair();
        BASE64Encoder encoder = new BASE64Encoder();
        BASE64Decoder decoder = new BASE64Decoder();
        String encoded = encoder.encode(keyPair.getPublic().getEncoded());
        AesKeyDto keyDto = restTemplate.postForObject("http://127.0.0.1:8084/google/rsakey", encoded, AesKeyDto.class);

        byte[] rsaEncryptedSecretKey = decoder.decodeBuffer(keyDto.getAesKey());
        byte[] ivector = decoder.decodeBuffer(keyDto.getIvector());
        byte[] secretKeyBytes = RSA.decrypt(rsaEncryptedSecretKey, keyPair.getPrivate());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        Scanner scanner = new Scanner(System.in);
        UserDto userDto = new UserDto();
        userDto.setSessionId(keyDto.getSessionId());
        System.out.println("Enter login:");
        //sergio.kiselev509@gmail.com
        String login = scanner.nextLine();
        System.out.println("Enter password:");
        //password
        String password = scanner.nextLine();
        userDto.setLogin(encoder.encode(AES.encrypt(login.getBytes(), secretKey, new IvParameterSpec(ivector))));
        userDto.setPassword(encoder.encode(AES.encrypt(password.getBytes(), secretKey, new IvParameterSpec(ivector))));
        VerifyDto verifyDto = restTemplate.postForObject("http://127.0.0.1:8084/google/login", userDto, VerifyDto.class);
        System.out.println(verifyDto);

        System.out.println("Enter code:");
        String code = scanner.nextLine();
        verifyDto.setCode(encoder.encode(AES.encrypt(code.getBytes(), secretKey, new IvParameterSpec(ivector))));
        String response = restTemplate.postForObject("http://127.0.0.1:8084/google/verify", verifyDto, String.class);
        System.out.println(response);
    }
}
