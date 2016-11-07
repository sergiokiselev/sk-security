package ch.rasc.sec;

import ch.rasc.sec.cypher.AES;
import ch.rasc.sec.cypher.RSA;
import ch.rasc.sec.dto.*;
import ch.rasc.sec.dto.restresponse.RestResponse;
import ch.rasc.sec.service.TOTPService;
import ch.rasc.sec.service.impl.TOTPServiceImpl;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
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

    private static BASE64Encoder encoder = new BASE64Encoder();
    private static BASE64Decoder decoder = new BASE64Decoder();
    private static RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args)
            throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InterruptedException {
        //testFullCryptography();
        //testWithoutPostCode();
        testWithoutAll();
    }

    private static void testWithoutAll() throws IOException, InterruptedException, NoSuchAlgorithmException, InvalidKeyException {
        RsaKeyDto rsaKeyDto = new RsaKeyDto();
        rsaKeyDto.setEncryption(false);
        rsaKeyDto.setPostCode(false);
        HttpEntity<RsaKeyDto> encodedEntity = new HttpEntity<>(rsaKeyDto);
        ResponseEntity<RestResponse<AesKeyDto>> keyDto = restTemplate
                .exchange("http://127.0.0.1:8080/rsakey",
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyDto>>() {});

        UserDto userDto = new UserDto();
        userDto.setSessionId(keyDto.getBody().getData().getSessionId());
        System.out.println("Enter login:");
        //
        String login = "sergio.kiselev509@gmail.com";//scanner.nextLine();
        System.out.println("Enter password:");
        //password
        String password = "password"; //scanner.nextLine();
        userDto.setLogin(encoder.encode(login.getBytes()));
        userDto.setPassword(encoder.encode(password.getBytes()));

        HttpEntity<UserDto> userDtoHttpEntity = new HttpEntity<>(userDto);
        ResponseEntity<RestResponse<VerifyDto>> verifyDto = restTemplate
                .exchange("http://127.0.0.1:8080/login",
                        HttpMethod.POST,
                        userDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<VerifyDto>>() {});

        System.out.println(verifyDto.getBody().getData());
        byte[] secretBytes = decoder.decodeBuffer(verifyDto.getBody().getData().getSecret());
        verifyToken(verifyDto.getBody().getData().getSessionId(), secretBytes);
    }

    private static void testWithoutPostCode() throws IOException, InterruptedException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        KeyPair keyPair = RSA.generateKeyPair();
        RsaKeyDto rsaKeyDto = new RsaKeyDto();
        String encoded = encoder.encode(keyPair.getPublic().getEncoded());

        rsaKeyDto.setRsaKey(encoded);
        rsaKeyDto.setEncryption(true);
        rsaKeyDto.setPostCode(false);
        HttpEntity<RsaKeyDto> encodedEntity = new HttpEntity<>(rsaKeyDto);
        ResponseEntity<RestResponse<AesKeyDto>> keyDto = restTemplate
                .exchange("http://127.0.0.1:8080/rsakey",
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyDto>>() {});

        byte[] rsaEncryptedSecretKey = decoder.decodeBuffer(keyDto.getBody().getData().getAesKey());
        byte[] encryptedIvector = decoder.decodeBuffer(keyDto.getBody().getData().getIvector());
        byte[] secretKeyBytes = RSA.decrypt(rsaEncryptedSecretKey, keyPair.getPrivate());
        byte[] ivector = RSA.decrypt(encryptedIvector, keyPair.getPrivate());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        Scanner scanner = new Scanner(System.in);
        ResponseEntity<RestResponse<VerifyDto>> verifyDto = login(keyDto, ivector, secretKey, scanner);
        System.out.println(verifyDto.getBody().getData());
        byte[] secretBytes = AES.decrypt(decoder.decodeBuffer(verifyDto.getBody().getData().getSecret()), secretKey, new IvParameterSpec(ivector));
        verifyToken(verifyDto.getBody().getData().getSessionId(), secretBytes);
    }

    private static void testFullCryptography() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InterruptedException {
        KeyPair keyPair = RSA.generateKeyPair();
        RsaKeyDto rsaKeyDto = new RsaKeyDto();
        String encoded = encoder.encode(keyPair.getPublic().getEncoded());

        rsaKeyDto.setRsaKey(encoded);
        rsaKeyDto.setEncryption(true);
        rsaKeyDto.setPostCode(true);
        HttpEntity<RsaKeyDto> encodedEntity = new HttpEntity<>(rsaKeyDto);
        ResponseEntity<RestResponse<AesKeyDto>> keyDto = restTemplate
                .exchange("http://127.0.0.1:8080/rsakey",
                        HttpMethod.POST,
                        encodedEntity,
                        new ParameterizedTypeReference<RestResponse<AesKeyDto>>() {});

        byte[] rsaEncryptedSecretKey = decoder.decodeBuffer(keyDto.getBody().getData().getAesKey());
        byte[] encryptedIvector = decoder.decodeBuffer(keyDto.getBody().getData().getIvector());
        byte[] secretKeyBytes = RSA.decrypt(rsaEncryptedSecretKey, keyPair.getPrivate());
        byte[] ivector = RSA.decrypt(encryptedIvector, keyPair.getPrivate());
        SecretKey secretKey = new SecretKeySpec(secretKeyBytes, "AES");
        Scanner scanner = new Scanner(System.in);
        ResponseEntity<RestResponse<VerifyDto>> verifyDto = login(keyDto, ivector, secretKey, scanner);
        System.out.println(verifyDto.getBody().getData());
        ResponseEntity<RestResponse<TotpSecretDto>> totpSecretDto = verifyCode(ivector, secretKey, scanner, verifyDto);
        byte[] secretBytes = AES.decrypt(decoder.decodeBuffer(totpSecretDto.getBody().getData().getSecret()), secretKey, new IvParameterSpec(ivector));
        System.out.println(secretBytes);
        verifyToken(totpSecretDto.getBody().getData().getSessionId(), secretBytes);
    }

    private static ResponseEntity<RestResponse<TotpSecretDto>> verifyCode(byte[] ivector, SecretKey secretKey, Scanner scanner, ResponseEntity<RestResponse<VerifyDto>> verifyDto)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        System.out.println("Enter code:");
        String code = scanner.nextLine();
        VerifyDto verifyDto1 = new VerifyDto();
        verifyDto1.setSessionId(verifyDto.getBody().getData().getSessionId());
        verifyDto1.setCode(encoder.encode(AES.encrypt(code.getBytes(), secretKey, new IvParameterSpec(ivector))));

        HttpEntity<VerifyDto> verifyDtoHttpEntity = new HttpEntity<>(verifyDto1);
        ResponseEntity<RestResponse<TotpSecretDto>> totpSecretDto = restTemplate
                .exchange("http://127.0.0.1:8080/verify",
                        HttpMethod.POST,
                        verifyDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<TotpSecretDto>>() {});
        System.out.println(totpSecretDto.getBody().getData());
        return totpSecretDto;
    }

    private static ResponseEntity<RestResponse<VerifyDto>> login(ResponseEntity<RestResponse<AesKeyDto>> keyDto, byte[] ivector, SecretKey secretKey, Scanner scanner)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        UserDto userDto = new UserDto();
        userDto.setSessionId(keyDto.getBody().getData().getSessionId());
        System.out.println("Enter login:");
        //
        String login = "sergio.kiselev509@gmail.com";//scanner.nextLine();
        System.out.println("Enter password:");
        //password
        String password = "password"; //scanner.nextLine();
        userDto.setLogin(encoder.encode(AES.encrypt(login.getBytes(), secretKey, new IvParameterSpec(ivector))));
        userDto.setPassword(encoder.encode(AES.encrypt(password.getBytes(), secretKey, new IvParameterSpec(ivector))));

        HttpEntity<UserDto> userDtoHttpEntity = new HttpEntity<>(userDto);
        return restTemplate
                .exchange("http://127.0.0.1:8080/login",
                        HttpMethod.POST,
                        userDtoHttpEntity,
                        new ParameterizedTypeReference<RestResponse<VerifyDto>>() {});
    }

    private static void verifyToken(String sessionId, byte[] secretBytes)
            throws InvalidKeyException, NoSuchAlgorithmException, InterruptedException {
        for (int i = 0; i < 10; i++) {
            long totpCode = getCurrentCode(secretBytes);
            System.out.println("CUrrent CODE: " + totpCode);
            TokenDto tokenDto = new TokenDto();
            tokenDto.setSessionId(sessionId);
            tokenDto.setToken(totpCode);

            HttpEntity<TokenDto> tokenDtoHttpEntity = new HttpEntity<>(tokenDto);
            ResponseEntity<RestResponse<String>> responseEntity = restTemplate
                    .exchange("http://127.0.0.1:8080/token",
                            HttpMethod.POST,
                            tokenDtoHttpEntity,
                            new ParameterizedTypeReference<RestResponse<String>>() {});
            System.out.println(responseEntity.getBody().getData());
            Thread.sleep(10000);
        }
    }

    private static long getCurrentCode(byte[] secretBytes) throws InvalidKeyException, NoSuchAlgorithmException {
        long timeIndex = System.currentTimeMillis() / 1000 / 30;
        TOTPService totpService = new TOTPServiceImpl();
        long totpCode = totpService.getCode(secretBytes, timeIndex);
        System.out.println(totpCode);
        return totpCode;
    }
}
